#include <stdio.h>
#include <string.h>
#include <zlib.h>

#define CHUNK 1024

/**
 * Compresses data using zlib's deflate algorithm.
 *
 * @param input The data to compress.
 * @param input_len The length of the input data.
 * @param output The output buffer to store the compressed data.
 * @param output_len The length of the compressed data.
 * @return 0 if successful, otherwise an error code.
 */
int compress_data(const char* input, size_t input_len, char* output, size_t* output_len) {
    z_stream strm;
    int ret;
    unsigned have;
    unsigned char out[CHUNK];

    // Initialize the stream
    strm.zalloc = Z_NULL;
    strm.zfree = Z_NULL;
    strm.opaque = Z_NULL;
    ret = deflateInit(&strm, Z_DEFAULT_COMPRESSION);
    if (ret != Z_OK) return ret;

    strm.avail_in = input_len;
    strm.next_in = (unsigned char*)input;
    strm.avail_out = CHUNK;
    strm.next_out = out;

    // Compress the data
    ret = deflate(&strm, Z_FINISH);
    if (ret != Z_STREAM_END) {
        deflateEnd(&strm);
        return ret == Z_OK ? Z_BUF_ERROR : ret;
    }

    // Copy the compressed data to the output buffer
    have = CHUNK - strm.avail_out;
    memcpy(output, out, have);
    *output_len = have;

    // Clean up
    deflateEnd(&strm);
    return Z_OK;
}

/**
 * Decompresses data using zlib's inflate algorithm.
 *
 * @param input The compressed data to decompress.
 * @param input_len The length of the compressed data.
 * @param output The output buffer to store the decompressed data.
 * @param output_len The length of the decompressed data.
 * @return 0 if successful, otherwise an error code.
 */
int decompress_data(const char* input, size_t input_len, char* output, size_t* output_len) {
    z_stream strm;
    int ret;
    unsigned have;
    unsigned char out[CHUNK];

    // Initialize the stream
    strm.zalloc = Z_NULL;
    strm.zfree = Z_NULL;
    strm.opaque = Z_NULL;
    ret = inflateInit(&strm);
    if (ret != Z_OK) return ret;

    strm.avail_in = input_len;
    strm.next_in = (unsigned char*)input;
    strm.avail_out = CHUNK;
    strm.next_out = out;

    // Decompress the data
    ret = inflate(&strm, Z_FINISH);
    if (ret != Z_STREAM_END) {
        inflateEnd(&strm);
        return ret == Z_OK ? Z_BUF_ERROR : ret;
    }

    // Copy the decompressed data to the output buffer
    have = CHUNK - strm.avail_out;
    memcpy(output, out, have);
    *output_len = have;

    // Clean up
    inflateEnd(&strm);
    return Z_OK;
}
