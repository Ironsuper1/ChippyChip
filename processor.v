module TOP (
  input instruction[31:0],
  output result
);

assign result = instruction[1];

endmodule


