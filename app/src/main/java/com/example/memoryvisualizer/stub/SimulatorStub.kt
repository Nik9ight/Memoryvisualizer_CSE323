package com.example.memoryvisualizer.stub

/**
 * Temporary deterministic simulator facade for UI scaffolding.
 * Person A will replace with real model integration later.
 */
class SimulatorStub {
    data class BlockStub(
        val id: String,
        val start: Int,
        val size: Int,
        val isFree: Boolean,
        val processId: String? = null,
        val internalFrag: Int = 0
    )
    data class ProcessStub(
        val id: String,
        val size: Int,
        val status: Status,
        val allocatedBlockId: String? = null
    ) {
        enum class Status { ALLOCATED, WAITING, FAILED }
    }
    data class StatsStub(
        val internalTotal: Int,
        val externalFree: Int,
        val largestFree: Int,
        val holeCount: Int,
        val successPct: Double
    )
    data class AllocationResultStub(
        val blocks: List<BlockStub>,
        val processes: List<ProcessStub>,
        val stats: StatsStub,
        val action: String
    )

    // Action log placeholder for future sealed class integration
    // sealed interface ActionLogEntry

    private var memorySize = 0
    private var initialBlocks: List<Int> = emptyList()
    private var processSizes: List<Int> = emptyList()
    private var strategy: Strategy = Strategy.FIRST

    private val snapshots = mutableListOf<AllocationResultStub>()
    private var cursor = -1 // points to current snapshot

    enum class Strategy { FIRST, BEST, WORST }

    fun load(blocks: List<Int>, processes: List<Int>) : AllocationResultStub {
        initialBlocks = blocks.filter { it > 0 }
        processSizes = processes.filter { it > 0 }
        memorySize = initialBlocks.sum()
        snapshots.clear()
        cursor = -1
        val start = snapshot(createInitialBlocks(), createInitialProcesses(), action = "LOAD")
        return start
    }

    fun setStrategy(s: Strategy) { strategy = s }

    fun current(): AllocationResultStub? = snapshots.getOrNull(cursor)

    fun step(): AllocationResultStub? {
        val cur = current() ?: return null
        val waiting = cur.processes.firstOrNull { it.status == ProcessStub.Status.WAITING } ?: return cur
        val blocks = cur.blocks.toMutableList()
        val idx = chooseBlock(blocks, waiting.size)
        val newProcs = cur.processes.map { p ->
            if (p.id == waiting.id) {
                if (idx == -1) p.copy(status = ProcessStub.Status.FAILED) else p.copy(status = ProcessStub.Status.ALLOCATED)
            } else p
        }.toMutableList()
        val action: String
        if (idx == -1) {
            action = "FAIL ${waiting.id}" // deterministic fail
        } else {
            val free = blocks[idx]
            val alloc = free.copy(id = free.id + ":${waiting.id}", size = waiting.size, isFree = false, processId = waiting.id)
            blocks[idx] = alloc
            val leftover = free.size - waiting.size
            if (leftover > 0) {
                blocks.add(idx + 1, free.copy(id = free.id + ":L", start = free.start + waiting.size, size = leftover))
            }
            action = "ALLOCATE ${waiting.id} via ${strategy.name}"
        }
        val normalized = normalize(blocks)
        val result = snapshot(normalized, newProcs, action)
        return result
    }

    fun runAll(): AllocationResultStub? {
        while (true) {
            val before = current() ?: return null
            val waiting = before.processes.any { it.status == ProcessStub.Status.WAITING }
            if (!waiting) return before
            step() ?: return before
        }
    }

    fun compact(): AllocationResultStub? {
        val cur = current() ?: return null
        val allocated = cur.blocks.filter { !it.isFree }.sortedBy { it.start }
        var ptr = 0
        val newBlocks = mutableListOf<BlockStub>()
        allocated.forEach { b ->
            newBlocks.add(b.copy(start = ptr))
            ptr += b.size
        }
        val free = memorySize - ptr
        if (free > 0) newBlocks.add(BlockStub(id = "F", start = ptr, size = free, isFree = true))
        val action = "COMPACT"
        return snapshot(normalize(newBlocks), cur.processes, action)
    }

    fun reset(): AllocationResultStub? = load(initialBlocks, processSizes)

    fun undo(): AllocationResultStub? {
        if (cursor > 0) cursor--
        return current()
    }

    fun redo(): AllocationResultStub? {
        if (cursor < snapshots.lastIndex) cursor++
        return current()
    }

    private fun chooseBlock(blocks: List<BlockStub>, size: Int): Int {
        val freeBlocks = blocks.withIndex().filter { it.value.isFree && it.value.size >= size }
        return when(strategy) {
            Strategy.FIRST -> freeBlocks.minByOrNull { it.index }?.index ?: -1
            Strategy.BEST -> freeBlocks.minByOrNull { it.value.size * 10 + it.value.start }?.index ?: -1
            Strategy.WORST -> freeBlocks.maxByOrNull { it.value.size * 10 - it.value.start }?.index ?: -1
        }
    }

    private fun createInitialBlocks(): List<BlockStub> {
        val list = mutableListOf<BlockStub>()
        var addr = 0
        initialBlocks.forEachIndexed { i, sz ->
            list += BlockStub(id = "B$i", start = addr, size = sz, isFree = true)
            addr += sz
        }
        return list
    }

    private fun createInitialProcesses(): List<ProcessStub> = processSizes.mapIndexed { i, sz ->
        ProcessStub(id = "P${i+1}", size = sz, status = ProcessStub.Status.WAITING)
    }

    private fun normalize(blocks: List<BlockStub>): List<BlockStub> = blocks.sortedBy { it.start }
        .fold(mutableListOf()) { acc, b ->
            val last = acc.lastOrNull()
            if (last != null && last.isFree && b.isFree && last.start + last.size == b.start) {
                acc[acc.lastIndex] = last.copy(size = last.size + b.size)
            } else acc += b
            acc
        }

    private fun snapshot(blocks: List<BlockStub>, processes: List<ProcessStub>, action: String): AllocationResultStub {
        val free = blocks.filter { it.isFree }
        val totalFree = free.sumOf { it.size }
        val largestFree = free.maxOfOrNull { it.size } ?: 0
        val externalFree = (totalFree - largestFree).coerceAtLeast(0)
        val holeCount = free.size
    val success = processes.count { it.status == ProcessStub.Status.ALLOCATED }
    val pct = if (processes.isEmpty()) 0.0 else success * 100.0 / processes.size
        val stats = StatsStub(
            internalTotal = 0,
            externalFree = externalFree,
            largestFree = largestFree,
            holeCount = holeCount,
            successPct = pct
        )
        val result = AllocationResultStub(blocks.map { it.copy() }, processes.map { it.copy() }, stats, action)
    // Truncate forward history if stepping after undo
    while (snapshots.lastIndex > cursor) snapshots.removeAt(snapshots.lastIndex)
        snapshots += result
        cursor = snapshots.lastIndex
        return result
    }
}
