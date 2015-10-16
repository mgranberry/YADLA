package com.kludgenics.util

/**
 * Created by matthias on 10/15/15.
 */

class Diff<T>(var old: MutableList<T>, private val isModified: (old: T, new: T) -> Boolean) {

    companion object {
        // These are not an enum for Android efficiency reasons
        val OP_REMOVE = 0
        val OP_MOVE = 1
        val OP_INSERT = 2
        val OP_MODIFY = 3
    }

    public data class Operation<T> (val operation: Int, val item: T, val from: Int, val to: Int = -1)
    public data class ChangeSet<T> (val operations: List<Operation<T>>)

    public fun update(new: List<T>): ChangeSet<T> {
        val operations = arrayListOf<Operation<T>>()
        old.forEach {
            if (!new.contains(it)) {
                operations.add(Operation(OP_REMOVE, it, old.indexOf(it)))
            }
        }
        operations.forEach { old.remove(it.item) }
        new.forEachIndexed { idx, it ->
            val oldPosition = old.indexOf(it)
            if (oldPosition >= 0) {
                val oldItem = old.get(oldPosition)
                if (oldPosition != idx) {
                    old.remove(oldPosition)
                    if (idx < old.size()) {
                        old.add(idx, oldItem)
                    } else
                        old.add(oldItem)
                    operations.add(Operation(OP_MOVE, it, oldPosition, idx))
                }
                if (!isModified(oldItem, it))
                    return@forEachIndexed
                else
                    operations.add(Operation(OP_MODIFY, it, idx))
            } else {
                operations.add(Operation(OP_INSERT, it, idx))
                if (idx < old.size()) {
                    old.add(idx, it)
                } else
                    old.add(it)
            }
        }
        return ChangeSet(operations)
    }
}