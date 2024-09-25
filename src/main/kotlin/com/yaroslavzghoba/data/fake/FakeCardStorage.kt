package com.yaroslavzghoba.data.fake

import com.yaroslavzghoba.data.model.CardStorage
import com.yaroslavzghoba.model.Card

/**
 * Represents a storage of cards in temporary memory.
 */
class FakeCardStorage : CardStorage {

    private val cards = mutableListOf<Card>()

    override suspend fun getById(id: Long): Card? {
        return cards.find { it.id == id }
    }

    override suspend fun getByCollectionId(id: Long): List<Card> {
        return cards.filter { it.collectionId == id }
    }

    override suspend fun insert(card: Card) {
        val isExists = cards.any { it.id == card.id }
        require(!isExists) { "A card with the same id is already exists" }
        cards.add(card)
    }

    override suspend fun update(card: Card) {
        val correspondingUser = cards.firstOrNull { it.id == card.id }
        requireNotNull(correspondingUser) { "A card with the same id is not found" }
        val index = cards.indexOf(correspondingUser)
        cards.removeAt(index = index)
        cards.add(index = index, element = card)
    }

    override suspend fun deleteById(id: Long) {
        val correspondingUser = cards.firstOrNull { it.id == id }
        correspondingUser?.let { user ->
            cards.remove(user)
        }
    }
}