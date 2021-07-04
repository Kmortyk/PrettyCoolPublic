package com.kmortyk.game.state

import com.kmortyk.game.item.Item
import com.kmortyk.game.item.ItemType

class PlayerItems : Iterable<Item?> {

    val items: Array<Item?> = Array(7 * 7) { null }
    val resources: MutableMap<String, Item> = mutableMapOf()

    public fun getResource(itemName: String) : Item? {
        return resources[itemName]
    }

    public operator fun get(idx: Int) : Item? {
        return items[idx]
    }

    public operator fun set(idx: Int, itm: Item?) {
        items[idx] = itm
    }

    public fun addItem(itm: Item) : Boolean {
        return if(itm.isResource())
            addResourceItem(itm)
        else
            addStandAloneItem(itm)
    }

    private fun addResourceItem(itm: Item) : Boolean {
        return if(itm.name in resources) {
            resources[itm.name]!!.count += itm.count
            true
        } else {
            addStandAloneItem(itm)
        }
    }

    private fun addStandAloneItem(itm: Item) : Boolean {
        for(i in items.indices) {
            if(items[i] == null) {
                items[i] = itm
                if(itm.isResource())
                    resources[itm.name] = items[i]!!
                return true
            }
        }
        return false // not found empty cells
    }

    public fun getResourceCount(resourceName: String) : Int {
        return if(resourceName !in resources)
            0
        else resources[resourceName]!!.count
    }

    public fun size() = items.size

    public override fun iterator(): Iterator<Item?> {
        return items.iterator()
    }
}