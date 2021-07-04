package com.kmortyk.game.quest

data class QuestData(
    // questID - identifies who gives this quest to the player
    // example:
    //      "reverti_know_rules"
    //      "dedevik_hat"
    val questID: String,
    // ID of the person that gives that quest
    val personID: String,
    // questType - type of this quest
    val questType: QuestType,
    // name - quest name shown in ui for the player convenience
    val name: String,
    // description - quest description which helps to describe what to do
    val description: String,
    // keysMap - used to store specific quest-data
    val keysMap: Map<String, String>
)