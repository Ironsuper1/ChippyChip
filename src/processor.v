module processor;

  wire clk, rst, en;
  reg [31:0] instruction = "11";
  reg [15:0] value_in = "12";
  reg [4:0] result;

  prog_count p1 (clk, rst, en);
  assign result = instruction[5:1] & value_in[5:1];

endmodule


module prog_count;
  parameter n = 32;
  input clk, rst, en;
  reg [n-1:0] Q;

  always @ (posedge rst, posedge clk)
    if (rst)
      Q <= 0;
    else if (en)
      Q <= Q + 1;

endmodule