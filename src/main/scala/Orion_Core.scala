package proc

import chisel3._
import chisel3.util._
import dataclass.data


// Execute
class Execute(width:UInt) extends Module {
    val io = IO(new Bundle {
        val reg1_in = Input(width)
        val reg2_in = Input(width)
        val imm_in = Input(width)
        val op = Input(UInt(5.W))
        val regcmd_in = Input(Bool())
        val pc_in = Input(width)
        val alu_result = Output(width)      // Output to next stage and branch check
        val pc_branch = Output(width)       // to PC Mux
    })

    val val_pick = UInt(4.W)
    if (io.regcmd_in == true.B) {
        val_pick := io.reg2_in
    } else {
        val_pick := io.imm_in
    }

    io.alu_result := MuxLookup(io.op, 0.U(32.W))(Seq(
        // R & I
        0.U(5.W) -> (io.reg1_in + val_pick),
        1.U(5.W) -> (io.reg1_in - val_pick),
        2.U(5.W) -> (io.reg1_in * val_pick),
        3.U(5.W) -> (io.reg1_in / val_pick),
        4.U(5.W) -> (io.reg1_in xorR val_pick),
        5.U(5.W) -> (io.reg1_in orR val_pick),
        6.U(5.W) -> (io.reg1_in andR val_pick),
        7.U(5.W) -> (io.reg1_in << val_pick),
        8.U(5.W) -> (io.reg1_in >> val_pick),
        9.U(5.W) -> (io.reg1_in >> val_pick), // MSD Extends?
        10.U(5.W) -> (io.reg1_in < val_pick),
        11.U(5.W) -> (io.reg1_in < val_pick), // Zero Extends?

        // I/S, Load/Store Addr Calc
        21.U(5.W) -> (io.reg1_in + val_pick), // byte(8bit), half(16bit), word(32bit)
        22.U(5.W) -> (io.reg1_in + val_pick), // byte, half -> Zero-Extends? Maybe replace 22,21 with addi

        // B Type
        23.U(5.W) -> (io.reg1_in === io.reg2_in),
        24.U(5.W) -> (io.reg1_in =/= io.reg2_in),
        25.U(5.W) -> (io.reg1_in < io.reg2_in),
        26.U(5.W) -> (io.reg1_in >= io.reg2_in),
        27.U(5.W) -> (io.reg1_in > io.reg2_in),
        28.U(5.W) -> (io.reg1_in >= io.reg2_in)
    ))

    io.pc_branch := io.pc_in + io.imm_in

}
// Mem
class dataMem(width:UInt) extends Module {
    val io = IO(new Bundle {
        val addr = Input(width)     // ALU Result
        val alu_result = Input(width)
        val write_enable = Input(Bool())
        val read_enable = Input(Bool())
        val mem_data = Output(width)
    })

    val data_mem = Mem(1024, UInt(32.W)) // 4KB data memory
      io.mem_data := 0.U

    when(io.write_enable) {
        // Write data to memory
        data_mem.write(io.addr, io.alu_result)
    }

    when(io.read_enable) {
        // Read data from memory to output
        io.mem_data := data_mem.read(io.addr)
    }
}

// Controller
class Controller(width:UInt) extends Module {
    val io = IO(new Bundle {
        val op_in = Input(UInt(4.W))
        val funct3 = Input(UInt(4.W))
        val funct7 = Input(UInt(7.W))
        val regcmd_out = Output(Bool())         // Execute Stage
        val write_mem = Output(Bool())          // Memory Stage
        val read_mem = Output(Bool())           // Memory Stage
        val branch_enable = Output(Bool())      // Branch Check
        val op_out = Output(UInt(5.W))          // ALU -> Execute Stage
        val wb_out = Output(Bool())
    })
    io.write_mem := false.B
    io.read_mem := false.B
    io.regcmd_out := false.B
    io.branch_enable := false.B
    def mapFunct3Op(funct3:UInt, default:UInt):UInt = {
        MuxLookup(funct3, default)(Seq(
            0.U -> 0.U(5.W),
            1.U -> 7.U(5.W),
            2.U -> 10.U(5.W),
            3.U -> 11.U(5.W),
            4.U -> 4.U(5.W),
            5.U -> 8.U(5.W),
            6.U -> 5.U(5.W),
            7.U -> 6.U(5.W)
        ))
    }

    def mapFunct3Branch(funct3:UInt, default:UInt):UInt = {
        MuxLookup(funct3, default)(Seq(
            0.U -> 23.U(5.W),
            1.U -> 24.U(5.W),
            4.U -> 25.U(5.W),
            5.U -> 26.U(5.W),
            6.U -> 27.U(5.W),
            7.U -> 28.U(5.W)
        ))
    }

    switch (io.op_in) {
        is(0.U) {     // R-Type
            io.regcmd_out := true.B
            io.op_out := Mux(io.funct7 === 0.U, mapFunct3Op(io.funct3, 0.U), Mux(io.funct3 === 0.U, 1.U, 9.U))
        }
        is(1.U) {     // I-Type
            // regcmd_out false
            io.op_out := mapFunct3Op(io.funct3, 0.U)
        }
        is(2.U) {     // I-Type (load)
            io.read_mem := true.B
        }
        is(3.U) {     // S-Type
            io.write_mem := true.B
        }
        is(4.U) {     // U-Type
            
        }
        is(5.U) {     // U-Type
            
        }
        is(6.U) {     // B-Type
            io.branch_enable := true.B
            io.op_out := mapFunct3Branch(io.funct3, 0.U)
        }
        is(7.U) {     // J-Type

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
    val execute = Module(new Execute(width))
    val mem = Module(new dataMem(width))

    // Fetch and Decode
    val ins = ins_mem.read(pc)
    val opcode = ins(6, 0)
    val rd = ins(11, 7)
    val funct3 = ins(14, 12)
    val rs1 = ins(19, 15)
    val rs2 = ins(24, 20)
    val funct7 = ins(31, 25)
    val imm = ins(31, 20)

    val reg1 = reg_file.read(rs1)
    val reg2 = reg_file.read(rs2)

    val ins_type = MuxLookup(opcode, 8.U(4.W))(Seq(
        "b0110011".U(7.W) -> 0.U(4.W),  // R
        "b0010011".U(7.W) -> 1.U(4.W),  // I
        "b0000011".U(7.W) -> 2.U(4.W),  // I
        "b0100011".U(7.W) -> 3.U(4.W),  // S
        "b0110111".U(7.W) -> 4.U(4.W),  // U
        "b0010111".U(7.W) -> 5.U(4.W),  // U
        "b1100011".U(7.W) -> 6.U(4.W),  // B
        "b1101111".U(7.W) -> 7.U(4.W)   // J
    ))

    // Controller inputs
    controller.io.op_in := ins_type
    controller.io.funct3 := funct3
    controller.io.funct7 := funct7


    // Execute inputs
    execute.io.reg1_in := reg1
    execute.io.reg2_in := reg2
    execute.io.imm_in := imm
    execute.io.op := controller.io.op_out
    execute.io.regcmd_in := controller.io.regcmd_out
    execute.io.pc_in := pc

    // Memory inputs
    mem.io.addr := execute.io.alu_result
    mem.io.alu_result := execute.io.alu_result
    mem.io.write_enable := controller.io.write_mem
    mem.io.read_enable := controller.io.read_mem


    // PC Increment
    pc := Mux(controller.io.branch_enable && (execute.io.alu_result).asBool, execute.io.pc_branch, pc + 4.U)

    // RegFile update / Writeback
    reg_file(rd) := Mux(controller.io.wb_out, execute.io.alu_result, mem.io.mem_data)
}
