#include <windows.h>
#include <stdio.h>

/**
 * Creates a memory-backed temporary file and returns a pointer to the memory region.
 * The caller must specify the size of the memory.
 *
 * @param size The size of the memory file in bytes.
 * @return A pointer to the allocated memory, or NULL on error.
 */
void* create_memory_file(size_t size) {
    HANDLE fileMapping = CreateFileMapping(INVALID_HANDLE_VALUE, NULL, PAGE_READWRITE,
                                           (DWORD)(size >> 32), (DWORD)(size & 0xFFFFFFFF), NULL);
    if (fileMapping == NULL) {
        printf("Failed to create file mapping. Error: %lu\n", GetLastError());
        return NULL;
    }

    void* memory = MapViewOfFile(fileMapping, FILE_MAP_WRITE, 0, 0, size);
    CloseHandle(fileMapping);

    if (memory == NULL) {
        printf("Failed to map view of file. Error: %lu\n", GetLastError());
    }

    return memory;
}

/**
 * Releases a memory-backed file.
 *
 * @param memory The pointer to the memory region.
 */
void release_memory_file(void* memory) {
    if (memory != NULL) {
        UnmapViewOfFile(memory);
    }
}
