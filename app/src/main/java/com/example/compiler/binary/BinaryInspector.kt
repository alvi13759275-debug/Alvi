package com.example.compiler.binary

import com.example.compiler.model.Instruction
import com.example.compiler.model.ProgramNode

data class HexRow(
    val offsetHex: String,
    val hexBytes: List<String>,
    val ascii: String,
    val section: String
)

data class BinarySection(
    val name: String,
    val startOffset: Int,
    val length: Int,
    val description: String
)

data class BinaryAnalysisReport(
    val formatName: String,
    val magicBytesHex: String,
    val totalBytes: Int,
    val sections: List<BinarySection>,
    val hexDump: List<HexRow>
)

object BinaryInspector {

    fun analyzeDex(instructions: List<Instruction>): BinaryAnalysisReport {
        // Construct a simulated real Dalvik/DEX container
        val byteList = mutableListOf<Byte>()

        // 1. DEX Header Magic: "dex\n039\0"
        val magic = byteArrayOf(0x64, 0x65, 0x78, 0x0A, 0x30, 0x33, 0x39, 0x00)
        byteList.addAll(magic.toList())

        // Checksum (4 bytes) + SHA-1 (20 bytes) placeholder
        for (i in 0 until 24) byteList.add(0x00)

        // File size (4 bytes little endian) placeholder
        val headerSize = 112
        byteList.add(0x70) // 112
        byteList.add(0x00)
        byteList.add(0x00)
        byteList.add(0x00)

        // Endian tag: 0x12345678 (little endian)
        byteList.addAll(listOf(0x78.toByte(), 0x56.toByte(), 0x34.toByte(), 0x12.toByte()))

        // Pad header to 112 bytes
        while (byteList.size < headerSize) {
            byteList.add(0x00)
        }

        val codeStart = byteList.size
        // Encode instructions as Dalvik bytecode
        for (instr in instructions) {
            val opByte = (instr.opcode.ordinal and 0xFF).toByte()
            byteList.add(opByte)
            val operandA = (instr.operandA as? Number)?.toInt() ?: 0
            byteList.add((operandA and 0xFF).toByte())
            byteList.add(((operandA shr 8) and 0xFF).toByte())
            byteList.add(0x00) // Alignment
        }

        // Align to 16 bytes
        while (byteList.size % 16 != 0) {
            byteList.add(0x00)
        }

        val sections = listOf(
            BinarySection("DEX Header", 0, headerSize, "Magic 'dex\\n039\\0', checksum, file size, endian tag"),
            BinarySection("String Table", headerSize, 16, "Offset map to interned string identifiers"),
            BinarySection("Bytecode Payload", codeStart, byteList.size - codeStart, "Dalvik register instructions & immediate pools")
        )

        val hexRows = mutableListOf<HexRow>()
        val bytes = byteList.toByteArray()

        for (i in bytes.indices step 16) {
            val rowBytes = mutableListOf<String>()
            val asciiSb = StringBuilder()

            for (j in 0 until 16) {
                if (i + j < bytes.size) {
                    val b = bytes[i + j]
                    rowBytes.add(String.format("%02X", b))
                    val char = b.toInt().toChar()
                    if (char in ' '..'~') {
                        asciiSb.append(char)
                    } else {
                        asciiSb.append('.')
                    }
                } else {
                    rowBytes.add("  ")
                    asciiSb.append(" ")
                }
            }

            val offsetHex = String.format("0x%04X", i)
            val section = when {
                i < headerSize -> "Header"
                i < codeStart -> "Strings"
                else -> "Code"
            }

            hexRows.add(HexRow(offsetHex, rowBytes, asciiSb.toString(), section))
        }

        return BinaryAnalysisReport(
            formatName = "Android Dalvik Executable (DEX)",
            magicBytesHex = "64 65 78 0A 30 33 39 00",
            totalBytes = bytes.size,
            sections = sections,
            hexDump = hexRows
        )
    }

    val KnownSignatures = listOf(
        Pair("DEX (Dalvik Executable)", "64 65 78 0A ('dex\\n')"),
        Pair("JVM Classfile", "CA FE BA BE"),
        Pair("WebAssembly (WASM)", "00 61 73 6D ('\\0asm')"),
        Pair("ELF Binary (Linux/Android)", "7F 45 4C 46 ('\\x7FELF')"),
        Pair("PE Executable (Windows)", "4D 5A ('MZ')"),
        Pair("Mach-O 64-bit (macOS/iOS)", "CF FA ED FE"),
        Pair("SPIR-V (Vulkan/GPU Shader)", "03 02 23 07")
    )
}
