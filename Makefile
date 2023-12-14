# Makefile for Verilator simulation and Yosys synthesis

# Verilator simulation variables
VERILOG_SRC_FOLDER = src/
VERILOG_TB = testbench.cpp
VERILATOR = verilator
VERILATOR_FLAGS = --cc
EXECUTABLE = sim
OBJ_DIR = obj_dir


# Yosys synthesis variables
SYNTH_VERILOG_SRC = src/processor.v
TARGET_MODULE = processor
OUTPUT_NETLIST = netlist_out.json
YOSYS = yosys

# Default make target
all: $(EXECUTABLE) $(OUTPUT_NETLIST)

# Target to build the Verilator simulation executable
$(EXECUTABLE): $(OBJ_DIR)/$(VERILOG_TB).o
	$(MAKE) -C $(OBJ_DIR) -f V$(TARGET_MODULE).mk

$(OBJ_DIR)/$(VERILOG_TB).o: $(VERILOG_SRC_FOLDER)/*.v $(VERILOG_TB)
	$(VERILATOR) $(VERILATOR_FLAGS) -y $(VERILOG_SRC_FOLDER) -I$(VERILOG_SRC_FOLDER) --cc $(VERILOG_TB) $(VERILOG_SRC_FOLDER)/*.v
	$(MAKE) -C $(OBJ_DIR) -f V$(TARGET_MODULE).mk

# Target to perform Yosys synthesis
$(OUTPUT_NETLIST): $(SYNTH_VERILOG_SRC)
	$(YOSYS) -p "read_verilog $(SYNTH_VERILOG_SRC); hierarchy -top $(TARGET_MODULE); proc; flatten $(TARGET_MODULE); synth -top $(TARGET_MODULE)"

# make a netlist for the design (ONLY WORKS FOR FULLY SELECTED DESIGNS)
netlist:
	$(YOSYS) -p "read_verilog $(SYNTH_VERILOG_SRC); hierarchy -top $(TARGET_MODULE); proc; flatten $(TARGET_MODULE); synth -top $(TARGET_MODULE) json $(OUTPUT_NETLIST)"

# Clean the project
clean:
	$(RM) -r $(OBJ_DIR) $(EXECUTABLE) $(OUTPUT_NETLIST)

# Clean and rebuild the project
rebuild: clean all

.PHONY: all clean rebuild