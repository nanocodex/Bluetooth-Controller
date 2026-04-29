package com.example.bluetoothcontroller.bluetooth

class MouseReportParser {
    companion object {
        /**
         * Creates a 4-byte HID Mouse Report
         * Byte 0: Buttons (Bit 0: Left, Bit 1: Right, Bit 2: Middle)
         * Byte 1: X Displacement (-127 to 127)
         * Byte 2: Y Displacement (-127 to 127)
         * Byte 3: Wheel (Scroll)
         */
        fun createReport(
            leftButton: Boolean,
            rightButton: Boolean,
            middleButton: Boolean,
            dX: Float,
            dY: Float,
            wheel: Int = 0
        ): ByteArray {
            // 1. Create the button bitmask
            var buttons = 0
            if (leftButton) buttons = buttons or 0x01
            if (rightButton) buttons = buttons or 0x02
            if (middleButton) buttons = buttons or 0x04

            // 2. Convert Float deltas to Signed Bytes (-127 to 127)
            // We use coerceIn to ensure we don't overflow the byte
            val x = dX.toInt().coerceIn(-127, 127).toByte()
            val y = dY.toInt().coerceIn(-127, 127).toByte()
            val w = wheel.coerceIn(-127, 127).toByte()

            return byteArrayOf(
                buttons.toByte(), // Byte 0
                x,                // Byte 1
                y,                // Byte 2
                w                 // Byte 3
            )
        }
    }
}