`define WIDTH 32

module processor;

  wire clk, rst, en;
  reg [31:0] instruction = "11";
  reg [15:0] value_in = "12";
  reg [4:0] result;
  reg mux_result;
  reg m2_Res;
  prog_count p1 (clk, rst, en, m2_Res);
  assign result = instruction[5:1] & value_in[5:1];
  mux one (clk, rst, result[0], en, mux_result);
endmodule;


module prog_count (
  input clk,
  input rst,
  input en,
  output pc
);
  parameter width = `WIDTH;
  reg [width-1:0] Q;
  always @ (posedge rst, posedge clk) begin
    if (rst) begin
      Q <= 0;
    end else if (en) begin
      Q <= Q + 1;
    end
  end
endmodule;

module ins_mem (
  input clk,
  input rst,
  input pc,
  output ins
);
  reg[width-1:0] imem[width-1:0];
  reg[width-1:0] count;
  initial begin
    
    count = 32'h0000;
  end 
  always @ (posedge rst, posedge clk) begin
    if (rst) begin
      imem[0] <= "1";
      count = 32'b0;
    end else begin
      count <= count + 1;
      ins <= imem[count];
    end
  end
endmodule;

module registers (
  input clk,
  input rw, // Reg Write
  input rst,
  input rr1,
  input rr2,
  input wr,
  input wd,
  output rd1,
  output rd2
);
  parameter rwidth = 6;
  reg[width-1:0] cpu_reg [width-1:0];
  integer rr1i = rr1;
  integer rr2i = rr2;
  integer wri = wr;
  integer wdi = wd;

  always @ (posedge rst, posedge clk) begin
    if (rst) begin
      //rst
    
    end else if (rw) begin
      // rw
    end else begin
      rd1 <= cpu_reg[rr1i];
      rd <= cpu_reg[rr2i];
    end
  end

endmodule;

module alu (
  input clk,
  input rst,
  input first,
  input second,
  input control,
  output val,
  output zero
);
  reg [width-1:0] temp;

  always @ (posedge rst, posedge clk) begin
    if (rst) begin
      zero <= "0";
    end else if (control) begin
      temp <= first + second;
      if (temp < first) begin
        zero = 1'b1;
      end
      val <= temp;
    end
  end


endmodule;

// module data_mem (
//   input clk,
//   input rst,
//   input addr,
//   input wd,
//   input mw,
//   input mr,
//   output rd
// );

//   always (posedge rst, posedge clk)
    


// endmodule;