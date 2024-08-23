package proc

import chisel3._
import chisel3.util._
import os.write

// Fetch
class Fetch(width:UInt) extends Module {
    val io =  IO(new Bundle {
        val pc_in = Input(width)
        val ins_out =  Output(width)
        val pc_out = Output(width)
    })
    
    val next_pc = io.pc_in + 4.U
    io.pc_out := next_pc
    io.ins_out := 0.U // Placeholder
}
// Decode
class Decode(width:UInt) extends Module {
    val io = IO(new Bundle {
        val ins_in = Input(width)
        val reg1_out = Output(width)
        val reg2_out = Output(width)
        val imm_out = Output(width)
        val rd_out =  Output(UInt(5.W))
        val type_out = Output(UInt(3.W))
    })
    // Start with R-Type, Split/Combine sections later...
    val opcode = io.ins_in(6,0)
    val rd = io.ins_in(11,7)
    val funct3 = io.ins_in(14,12)
    val rs1 = io.ins_in(19,15)
    val rs2 = io.ins_in(24,20)
    val funct7 = io.ins_in(31,25)

    io.type_out := MuxLookup(opcode, 6.U(3.W))(Seq(
        "b0110011".U(7.W) -> 0.U(3.W),
        "b0010011".U(7.W) -> 1.U(3.W),
        "b0100011".U(7.W) -> 2.U(3.W),
        "b0110111".U(7.W) -> 3.U(3.W),
        "b0010111".U(7.W) -> 3.U(3.W),
        "b1100011".U(7.W) -> 4.U(3.W),
        "b1101111".U(7.W) -> 5.U(3.W)
    ))
}
// Execute
class Execute(width:UInt) extends Module {
    val io = IO(new Bundle {
        val reg1_in = Input(width)
        val reg2_in = Input(width)
        val imm_in = Input(width)
        val op = Input(UInt(5.W))
        val alu_result = Output(width)
    })


    io.alu_result := MuxLookup(io.op, 0.U(32.W))(Seq(
        // R
        0.U(5.W) -> (io.reg1_in + io.reg2_in),
        1.U(5.W) -> (io.reg1_in - io.reg2_in),
        2.U(5.W) -> (io.reg1_in * io.reg2_in),
        3.U(5.W) -> (io.reg1_in / io.reg2_in),
        4.U(5.W) -> (io.reg1_in xorR io.reg2_in),
        5.U(5.W) -> (io.reg1_in orR io.reg2_in),
        6.U(5.W) -> (io.reg1_in andR io.reg2_in),
        7.U(5.W) -> (io.reg1_in << io.reg2_in),
        8.U(5.W) -> (io.reg1_in >> io.reg2_in),
        9.U(5.W) -> (io.reg1_in >> io.reg2_in), // MSD Extends?
        10.U(5.W) -> (io.reg1_in < io.reg2_in),
        11.U(5.W) -> (io.reg1_in < io.reg2_in), // Zero Extends?
        // I
        12.U(5.W) -> (io.reg1_in + io.imm_in),
        13.U(5.W) -> (io.reg1_in xorR io.imm_in),
        14.U(5.W) -> (io.reg1_in orR io.imm_in),
        15.U(5.W) -> (io.reg1_in andR io.imm_in),
        16.U(5.W) -> (io.reg1_in << io.imm_in),
        17.U(5.W) -> (io.reg1_in >> io.imm_in),
        18.U(5.W) -> (io.reg1_in >> io.imm_in), // MSB Extends?
        19.U(5.W) -> (io.reg1_in < io.imm_in),
        20.U(5.W) -> (io.reg1_in < io.imm_in),   // Zero Extends?

        // I/S, Load/Store Addr Calc
        21.U(5.W) -> (io.reg1_in + io.imm_in), // byte(8bit), half(16bit), word(32bit)
        22.U(5.W) -> (io.reg1_in + io.imm_in), // byte, half -> Zero-Extends? Maybe replace 22,21 with addi
    ))
}
// Mem
class dataMem(width:UInt) extends Module {
    val io = IO(new Bundle {
        val alu_result = Input(width)
        val mem_data = Output(width)
    })

    val data_mem = Mem(1024, UInt(32.W)) // 4KB data memory

}
// WriteBack
class writeBack(width:UInt) extends Module {
    val io = IO(new Bundle {
        val mem_data = Input(width)
        val rd_in = Input(width)
        val reg_write = Output(width)
    })

}

class Controller(width:UInt) extends Module {
    val io = IO(new Bundle {
        val op_in = Input(UInt(3.W))
        val funct3 = Input(UInt(4.W))
        val funct7 = Input(UInt(7.W))
        val op_out = Output(UInt(5.W))
    })
    switch (io.op_in) {
        is(0.U) {     // R-Type
            switch (io.funct7) {
                is(0.U(7.W)) {
                    if (io.funct3 == 0.U(4.W)) {
                        io.op_out := 0.U(5.W)
                    } else if (io.funct3 == 1.U(4.W)) {
                        io.op_out := 7.U(5.W)
                    } else if (io.funct3 == 2.U(4.W)) {
                        io.op_out := 10.U(5.W)
                    } else if (io.funct3 == 3.U(4.W)) {
                        io.op_out := 11.U(5.W)
                    } else if (io.funct3 == 4.U(4.W)) {
                        io.op_out := 4.U(5.W)
                    } else if (io.funct3 == 5.U(4.W)) {
                        io.op_out := 8.U(5.W)
                    } else if (io.funct3 == 6.U(4.W)) {
                        io.op_out := 5.U(5.W)
                    } else if (io.funct3 == 7.U(4.W)) {
                        io.op_out := 6.U(5.W)
                    }
                }
                is(32.U(7.W)) {
                    if (io.funct3 == 0.U(4.W)) {
                        io.op_out := 1.U(5.W)
                    } else {
                        io.op_out := 9.U(5.W)
                    }
                }
            }
        }
        is(1.U) {     // I-Type
            
        }
        is(2.U) {     // S-Type
            
        }
        is(3.U) {     // U-Type
            
        }
        is(4.U) {     // B-Type
            
        }
        is(5.U) {     // J-Type
            
        }
    }
}

class Orion_Core extends Module {
    val io = IO(new Bundle {
        
    })

    val width = UInt(32.W)  // Define Width
    val reg_file = Mem(32, UInt(32.W))
    val ins_mem = Mem(1024, UInt(32.W)) // 4KB instruction memory
    val pc = RegInit(0.U(32.W))
    
    
    val controller = Module(new Controller(width))
    val fetch = Module(new Fetch(width))
    val decode = Module(new Decode(width))
    val execute = Module(new Execute(width))
    val mem = Module(new dataMem(width))
    val writeback = Module(new writeBack(width))


    // Link
    fetch.io.pc_in := pc
    decode.io.ins_in := fetch.io.ins_out

    controller.io.op_in := decode.io.type_out
    
    execute.io.reg1_in := decode.io.reg1_out
    execute.io.reg2_in := decode.io.reg2_out
    
    mem.io.alu_result := execute.io.alu_result
    
    writeback.io.mem_data := mem.io.mem_data
    writeback.io.rd_in := decode.io.rd_out
    // PC Increment
    pc := fetch.io.pc_out
    // RegFile update
    reg_file(writeback.io.rd_in) := writeback.io.reg_write
}



