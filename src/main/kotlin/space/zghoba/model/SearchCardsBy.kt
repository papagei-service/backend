package space.zghoba.model

sealed class SearchCardsBy {

    data class User(val userId: Long): SearchCardsBy()

    data class Collection(val collectionId: Long): SearchCardsBy()
}