package proc

import chisel3._
import chisel3.util._

// Fetch
class Fetch {

}
// Decode
class Decode {

}
// Execute
class Execute {

}
// Mem
class Mem {

}
// WriteBack
class WriteBack {

}



class Orion_Core extends Module {
    val io = IO(new Bundle {
        val mem = Flipped(new ImemPortIO())
        val exit = Output(Bool())
    })
    val reg_file = Mem(32, UInt(32.W))
    val fetch = new Fetch()
    val decode = new Decode()
    val execute = new Execute()
    val mem = new Mem()
    val writeback = new WriteBack()
}
