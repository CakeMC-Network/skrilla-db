#include <stdlib.h>
#include <time.h>
#include <stdint.h>
#include <uuid/uuid.h>

/**
 * Generates a random UUID and writes it into the provided buffer.
 * The buffer must be at least 37 bytes to hold the UUID string.
 */
void generate_uuid(char* buffer) {
    uuid_t uuid;
    uuid_generate(uuid);
    uuid_unparse(uuid, buffer);
}

/**
 * Generates a random long integer.
 */
int64_t generate_random_long() {
    return ((int64_t)rand() << 32) | rand();
}

// Initialize the random seed for better randomness
__attribute__((constructor)) void init_seed() {
    srand((unsigned int)time(NULL));
}