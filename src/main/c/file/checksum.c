#include <stdio.h>
#include <string.h>
#include <openssl/sha.h>

/**
 * Calculates the SHA-256 checksum of a file.
 *
 * @param filename The path of the file to checksum.
 * @param checksum Output buffer to store the SHA-256 checksum in hex format (64 bytes).
 * @return 0 on success, -1 on error.
 */
int calculate_file_sum(const char* filename, char* checksum) {
    FILE *file = fopen(filename, "rb");
    if (!file) {
        perror("Failed to open file");
        return -1;
    }

    SHA256_CTX sha256_ctx;
    SHA256_Init(&sha256_ctx);

    unsigned char buffer[4096];
    size_t bytesRead;
    while ((bytesRead = fread(buffer, 1, sizeof(buffer), file)) > 0) {
        SHA256_Update(&sha256_ctx, buffer, bytesRead);
    }

    unsigned char hash[SHA256_DIGEST_LENGTH];
    SHA256_Final(hash, &sha256_ctx);

    // Convert hash to hex format
    for (int i = 0; i < SHA256_DIGEST_LENGTH; i++) {
        sprintf(checksum + (i * 2), "%02x", hash[i]);
    }

    fclose(file);
    return 0;
}

/**
 * Validates a file by comparing its SHA-256 checksum to a provided checksum.
 *
 * @param filename The path of the file to validate.
 * @param expected_checksum The expected checksum to compare against.
 * @return 1 if the file matches the checksum, 0 if not, -1 on error.
 */
int validate_file(const char* filename, const char* expected_checksum) {
    char calculated_checksum[65]; // 64 characters for SHA-256 hash + null terminator
    int result = calculate_file_sum(filename, calculated_checksum);
    if (result != 0) {
        return -1;
    }

    return strcmp(expected_checksum, calculated_checksum) == 0 ? 1 : 0;
}
