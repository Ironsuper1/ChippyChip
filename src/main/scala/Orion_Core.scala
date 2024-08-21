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
    
    // switch (opcode) {
    //     is("b0110011".U(7.W)) {     // R-Type

    //     }
    //     is("b0010011".U(7.W)) {     // I-Type
            
    //     }
    //     is("b0100011".U(7.W)) {     // S-Type
            
    //     }
    //     is("b0110111".U(7.W) | "b0010111".U(7.W)) {     // U-Type
            
    //     }
    //     is("b1100011".U(7.W)) {     // B-Type
            
    //     }
    //     is("b1101111".U(7.W)) {     // J-Type
            
    //     }
    // }
}
// Execute
class Execute(width:UInt) extends Module {
    val io = IO(new Bundle {
        val reg1_in = Input(width)
        val reg2_in = Input(width)
        val alu_result = Output(width)
    })

}
// Mem
class dataMem(width:UInt) extends Module {
    val io = IO(new Bundle {
        val alu_result = Input(width)
        val mem_data = Output(width)
    })

}
// WriteBack
class writeBack(width:UInt) extends Module {
    val io = IO(new Bundle {
        val mem_data = Input(width)
        val rd_in = Input(width)
        val reg_write = Output(width)
    })

}



class Orion_Core extends Module {
    val io = IO(new Bundle {
        
    })

    val width = UInt(32.W)  // Define Width
    val reg_file = Mem(32, UInt(32.W))
    val ins_mem = Mem(1024, UInt(32.W)) // 4KB instruction memory
    val pc = RegInit(0.U(32.W))
    

    val fetch = Module(new Fetch(width))
    val decode = Module(new Decode(width))
    val execute = Module(new Execute(width))
    val mem = Module(new dataMem(width))
    val writeback = Module(new writeBack(width))


    // Link
    fetch.io.pc_in := pc
    decode.io.ins_in := fetch.io.ins_out
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



class ALU (bitWidth: Int) {
    val io = IO(new Bundle {
        val x = Input(UInt(bitWidth.W))
        val y = Input(UInt(bitWidth.W))
        val op = Input(UInt(4.W))
        val out = Output(UInt());
    })

    if (io.op == 0) {
        io.out := io.x+io.y
    } else if (io.op == 1) {
        io.out := io.x-io.y
    } else if (io.op == 2) {
        io.out := io.x*io.y
    } else if (io.op == 3) {
        io.out := io.x/io.y
    }
}

