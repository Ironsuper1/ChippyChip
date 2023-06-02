module processor;

  wire clk, rst;
  reg [31:0] instruction = "11";
  reg [15:0] value_in = "12";
  reg [4:0] result;

  assign result = instruction[5:1] & value_in[5:1];

endmodule
