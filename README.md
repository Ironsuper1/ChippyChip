# ChippyChip V2

##### Remake of ChippyChip in Chisel HDL instead of Verilog

#### Currently using the Chisel Language
#### Progression: RV32I -> RV64I -> FP Support -> Superscalar -> Out-Of-Order


#### TODO: Add to testbench
#### Fix fully selected design issue so a json netlist can be generated
#### Use something like this: https://github.com/nturley/netlistsvg to generate an SVG based on netlist

![Alt text](gpu_modules2.png)




ROHD?: https://intel.github.io/rohd-website/
- 4 (0,1,X,Z) Value support vs 2 value in verilator
- Dart based
- Requires new lang (not verilog/vhdl)
- Supports cosim with verilog
