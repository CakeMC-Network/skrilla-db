package net.cakemc.skrilla.networking.packet

enum class PacketIdentity {

    // AUTH
    AUTH_REQUEST,
    AUTH_RESPONSE,

    // SYSTEM

    // get all collections
    GET_COLLECTIONS, //
    COLLECTIONS_REPL, //

    // create collection
    CREATE_COLLECTION, //
    CREATE_COLLECTION_STATUS, //

    // documents size
    GET_DOCUMENT_SIZE, //
    DOCUMENT_SIZE_REPL, //

    // create document
    INSERT_DOCUMENT, //
    INSERT_DOCUMENT_STATUS, //

    // delete comment
    DELETE_DOCUMENT, //
    DELETE_DOCUMENT_STATUS, //

    // replace document
    REPLACE_DOCUMENT, //
    REPLACE_DOCUMENT_STATUS, //

    // document update
    UPDATE_DOCUMENT, //
    UPDATE_DOCUMENT_STATUS, //

    // filter
    FIND_DOCUMENT, //
    DOCUMENT_REPLY,

    // todo multi document, cursors

    ;
}