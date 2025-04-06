package net.cakemc.database

import net.cakemc.database.api.DatabaseRecord
import net.cakemc.database.api.Document
import net.cakemc.database.collection.Collection
import net.cakemc.database.collection.DocumentCollection
import net.cakemc.database.filter.Filters
import net.cakemc.database.serial.AbstractRead
import net.cakemc.database.serial.AbstractWrite
import net.cakemc.database.serial.impl.BinCollectionReader
import net.cakemc.database.serial.impl.BinCollectionWriter
import net.cakemc.skrilla.database.CollectionInfo
import net.cakemc.skrilla.database.Index
import net.cakemc.skrilla.database.IndexEntry
import net.cakemc.skrilla.natives.file.FileUtility
import net.cakemc.skrilla.natives.file.FileUtilityFactory
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom

/**
 * The type Default database.
 */
open class DefaultDatabase(val folder: Path) : AbstractDatabase() {
    private val collectionMap: MutableMap<String, Collection<DatabaseRecord>> =
        ConcurrentHashMap()

    private val databaseFolder = folder.apply { Files.createDirectories(folder) }

    private val collectionWriter: AbstractWrite = BinCollectionWriter()
    private val collectionReader: AbstractRead = BinCollectionReader()

    private val fileUtility: FileUtility = FileUtilityFactory.create()

    private val indexFile = Path.of("index.idx")
    private val walFile = Path.of("wal.txt")

    // Index map: document ID -> (file name, offset in the file)
    private val indexes: MutableMap<String, Index> = ConcurrentHashMap()

    private val MAX_DOCUMENTS_PER_FILE = 10000

    override fun getCollection(name: String): Collection<DatabaseRecord> {
        if (collectionMap.containsKey(name)) {
            return collectionMap[name]!!
        }
        val collection = DocumentCollection(ArrayList(), nextFreeId(), name)
        collectionMap[collection.name] = collection
        return collection
    }

    override fun getCollections(): List<Collection<DatabaseRecord>> {
        return collectionMap.values.toList()
    }

    private fun nextFreeId(): Long {
        val current = ThreadLocalRandom.current().nextLong()

        if (collectionMap.entries.stream()
                .anyMatch { entry: Map.Entry<String, Collection<DatabaseRecord>> -> entry.value.id == current }
        ) return nextFreeId()

        return current
    }

    override fun save() {
        for ((key, value) in collectionMap) {
            val collectionFolder = Paths.get(folder.toString(), "/$key/")
            Files.createDirectories(collectionFolder)

            for (document in value.collect()) {
                appendToWAL(collectionFolder, value.name, document)
            }

            val collectionFiles = mutableListOf<Path>()
            var documentCount = 0
            var currentDataFileIndex = 1
            var currentDataFilePath = collectionFolder.resolve("data-${currentDataFileIndex}.db_bin")
            val dataBuilder = mutableListOf<ByteArray>()

            for (document in value.collect()) {
                val data = collectionWriter.serializeDocument(document)
                val compressedData = DEFAULT_COMPRESSION.compress(data)
                dataBuilder.add(compressedData)

                documentCount++

                if (documentCount >= MAX_DOCUMENTS_PER_FILE) {
                    collectionFiles.add(currentDataFilePath)
                    saveToFile(currentDataFilePath, dataBuilder)
                    documentCount = 0
                    dataBuilder.clear()
                    currentDataFileIndex++
                    currentDataFilePath = collectionFolder.resolve("data-${currentDataFileIndex}.db_bin")
                }
            }

            // Save any remaining documents
            if (dataBuilder.isNotEmpty()) {
                collectionFiles.add(currentDataFilePath)
                saveToFile(currentDataFilePath, dataBuilder)
            }

            updateIndexFile(collectionFolder, value, collectionFiles.map { it.fileName })

            // Save info file
            val infoFile = collectionFolder.resolve("info.meta")
            val now = System.currentTimeMillis()
            val collectionInfo = CollectionInfo(
                createTime = now,
                lastWriteTime = now,
                lastReadTime = 0L,
                collectionId = value.id
            )

            val infoBytes = serializeInfo(collectionInfo)
            fileUtility.saveFile(infoFile.toString(), infoBytes)

            clearWAL(collectionFolder)
        }
    }

    fun saveSingleDocument(document: Document, collectionName: String) {
        val collection = getCollection(collectionName)
        val collectionFolder = folder.resolve(collectionName)
        Files.createDirectories(collectionFolder)

        val documentId = document.id
        val index = indexes.getOrPut(collectionName) { Index(LinkedList()) }

        // Serialize and compress the document
        val serialized = collectionWriter.serializeDocument(document)
        val compressed = DEFAULT_COMPRESSION.compress(serialized)

        // Determine the file and offset
        var indexEntry = index.entries.find { it.documentId == documentId }

        // If document is new or index entry is missing, append to last file
        if (indexEntry == null) {
            // Determine the next file to append
            val lastFileIndex = index.entries.maxOfOrNull { it.offset / MAX_DOCUMENTS_PER_FILE } ?: 0
            val nextOffset = index.entries.count { it.fileName == "data-${lastFileIndex + 1}.db_bin" }

            val fileName = "data-${lastFileIndex + 1}.db_bin"
            val dataFilePath = collectionFolder.resolve(fileName)

            // Append the document to the file
            Files.write(dataFilePath, compressed, StandardOpenOption.CREATE, StandardOpenOption.APPEND)

            indexEntry = IndexEntry(documentId, fileName, nextOffset.toLong())
            index.entries.add(indexEntry)
        } else {
            // Overwrite by rewriting whole file (simplified, consider improving this)
            val dataFilePath = collectionFolder.resolve(indexEntry.fileName)
            val existingRaw = fileUtility.readFile(dataFilePath.toString())
            val decompressed = DEFAULT_COMPRESSION.decompress(existingRaw)

            val buffer = DataInputStream(ByteArrayInputStream(decompressed))
            val documents = ArrayList<DatabaseRecord>()

            while (buffer.available() > 0) {
                val doc = collectionReader.readElement(buffer)
                documents.add(if (doc.id == documentId) document else doc)
            }

            val reSerialized = documents.map {
                DEFAULT_COMPRESSION.compress(collectionWriter.serializeDocument(it as Document))
            }.reduce { acc, bytes -> acc + bytes }

            fileUtility.saveFile(dataFilePath.toString(), reSerialized)
        }

        updateIndexFile(collectionFolder, collection, index.entries.map { Path.of(it.fileName) })

        // Update metadata
        val infoFile = collectionFolder.resolve("info.meta")
        val now = System.currentTimeMillis()
        val info = if (Files.exists(infoFile)) {
            deserializeInfo(fileUtility.readFile(infoFile.toString()))
        } else {
            CollectionInfo(createTime = now, lastWriteTime = now, lastReadTime = 0, collectionId = collection.id)
        }
        val updatedInfo = info.copy(lastWriteTime = now)
        fileUtility.saveFile(infoFile.toString(), serializeInfo(updatedInfo))
    }


    private fun saveToFile(filePath: Path, data: List<ByteArray>) {
        val compressed = data.reduce { acc, byteArray -> acc + byteArray }
        fileUtility.saveFile(filePath.toString(), compressed)
    }

    private fun updateIndexFile(collectionFolder: Path, collection: Collection<DatabaseRecord>, collectionFiles: List<Path>) {
        val collectionIndex = collectionFolder.resolve(indexFile)

        val indexData = mutableListOf<String>()

        for (file in collectionFiles) {
            val fileName = file.toString()
            val fileOffset = collectionFiles.indexOf(file) * MAX_DOCUMENTS_PER_FILE
            for (document in collection.collect()) {
                val indexLine = "${document.id}=$fileName:$fileOffset"
                indexData.add(indexLine)
            }
        }

        val rawBytes = indexData.joinToString("\n").toByteArray(Charset.defaultCharset())
        val compressed = DEFAULT_COMPRESSION.compress(rawBytes)
        fileUtility.saveFile(collectionIndex.toString(), compressed)
    }

    override fun load() {
        Files.list(folder).forEach { collectionFolder ->

            recoverFromWAL(collectionFolder)

            val collectionName = collectionFolder.getName(2).toString()
            val index = loadIndex(collectionFolder, collectionName) // todo maybe use for fast read

            Files.list(collectionFolder).filter {it.fileName.toString().contains("data")}.forEach {

                val rawBytes = fileUtility.readFile(it.toString())
                val decompressed = DEFAULT_COMPRESSION.decompress(rawBytes)

                val list = ArrayList<DatabaseRecord>()

                val buffer = DataInputStream(ByteArrayInputStream(decompressed))
                while (buffer.available() != 0) {
                    val document = collectionReader.readElement(buffer)
                    list.add(document)
                }

                val infoFile = collectionFolder.resolve("info.meta")
                val info = if (Files.exists(infoFile)) {
                    deserializeInfo(fileUtility.readFile(infoFile.toString()))
                } else {
                    CollectionInfo(System.currentTimeMillis(), 0, 0, nextFreeId())
                }

                // Update lastReadTime
                val updatedInfo = info.copy(lastReadTime = System.currentTimeMillis())
                fileUtility.saveFile(infoFile.toString(), serializeInfo(updatedInfo))

                collectionMap[collectionName] = DocumentCollection(list, updatedInfo.collectionId, collectionName)

            }
        }
    }

    private fun loadIndex(collectionPath: Path, collectionName: String): Index {
        val indexPath = collectionPath.resolve(indexFile)
        val rawBytes = Files.readAllBytes(indexPath)
        val decompressed = DEFAULT_COMPRESSION.decompress(rawBytes)
        val lines = String(decompressed).split("\n")

        val index = Index(LinkedList())
        lines.forEach {
            val parts = it.split("=")
            val id = parts[0].toLong()
            val fileOffset = parts[1].split(":")
            val fileName = fileOffset[0]
            val offset = fileOffset[1].toLong()

            val indexEntry = IndexEntry(id, fileName, offset)
            index.entries.add(indexEntry)
        }

        indexes.put(collectionName, index)
        return index
    }

    private fun deserializeInfo(bytes: ByteArray): CollectionInfo {
        val split = String(bytes, Charsets.UTF_8).split(",")
        return CollectionInfo(
            createTime = split[0].toLong(),
            lastWriteTime = split[1].toLong(),
            lastReadTime = split[2].toLong(),
            collectionId = split[3].toLong()
        )
    }

    private fun serializeInfo(info: CollectionInfo): ByteArray {
        val joined = listOf(
            info.createTime,
            info.lastWriteTime,
            info.lastReadTime,
            info.collectionId
        ).joinToString(separator = ",")
        return joined.toByteArray(Charsets.UTF_8)
    }

    private fun appendToWAL(collectionPath: Path, collectionName: String, document: Document) {
        val walFile = collectionPath.resolve(walFile)

        val serialized = collectionWriter.serializeDocument(document)
        val base64Data = Base64.getEncoder().encodeToString(serialized)
        val line = "$collectionName;${document.id};$base64Data\n"

        Files.writeString(walFile, line, StandardOpenOption.CREATE, StandardOpenOption.APPEND)
    }

    private fun recoverFromWAL(collectionPath: Path) {
        val walFile = collectionPath.resolve(walFile)

        if (!Files.exists(walFile))
            fileUtility.saveFile(walFile.toString(), "".toByteArray(Charset.defaultCharset()))

        Files.readAllLines(walFile).forEach { line ->
            val parts = line.split(";")
            if (parts.size != 3) return@forEach

            val collectionName = parts[0]
            val documentId = parts[1].toLong()
            val base64Data = parts[2]
            val data = Base64.getDecoder().decode(base64Data)
            val document = collectionReader.readElement(DataInputStream(ByteArrayInputStream(data)))

            val collection = getCollection(collectionName)
            collection.replaceOneDocument(Filters.id(documentId), document)
        }
    }

    private fun clearWAL(collectionPath: Path) {
        val walFile = collectionPath.resolve(walFile)
        fileUtility.saveFile(walFile.toString(), "".toByteArray(Charset.defaultCharset()))
    }

}
