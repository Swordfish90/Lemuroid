package com.codebutler.retrograde.storage.archiveorg.model

data class AdvancedSearchResponse(val response: Response) {
    data class Response(val docs: List<Doc>) {
        data class Doc(val identifier: String)
    }
}
