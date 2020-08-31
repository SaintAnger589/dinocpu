// This file is where all of the CPU components are assembled into the whole CPU

package dinocpu

import chisel3._
import chisel3.util._
import dinocpu.components._

/**
 * The main CPU definition that hooks up all of the other components.
 *
 * For more information, see section 4.4 of Patterson and Hennessy
 * This follows figure 4.21
 */
class SingleCycleCPU(implicit val conf: CPUConfig) extends BaseCPU {

  // All of the structures required
  val pc         = RegInit(0.U)
  val control    = Module(new Control())
  val registers  = Module(new RegisterFile())
  val csr        = Module(new CSRRegFile())
  val aluControl = Module(new ALUControl())
  val alu        = Module(new ALU())
  val immGen     = Module(new ImmediateGenerator())
  val branchCtrl = Module(new BranchControl())
  val pcPlusFour = Module(new Adder())
  val branchAdd  = Module(new Adder())
  val (cycleCount, _) = Counter(true.B, 1 << 30)

  // To make the FIRRTL compiler happy. Remove this as you connect up the I/O's
  //control.io    := DontCare
  //registers.io  := DontCare
  //aluControl.io := DontCare
  //alu.io        := DontCare
  immGen.io     := DontCare
  branchCtrl.io := DontCare
  pcPlusFour.io := DontCare
  branchAdd.io  := DontCare

  io.imem.address := pc

  val instruction = io.imem.instruction

 //instruction memory
  //io.imem.address   := pc
  io.imem.valid     := true.B
  //data memory
  io.dmem.address   := alu.io.result
  io.dmem.writedata := registers.io.readdata2
  io.dmem.memread   := control.io.memread
  io.dmem.memwrite  := control.io.memwrite
  io.dmem.maskmode  := instruction(13,12)
  io.dmem.sext      := ~instruction(14)

  val write_data = Wire(UInt())
  when (control.io.toreg === 1.U) {
    write_data := io.dmem.readdata
  }.elsewhen(control.io.toreg === 2.U){
    write_data := pcPlusFour.io.result
  }.elsewhen(control.io.toreg === 3.U) {
    write_data := csr.io.write_data
  } .otherwise {
    write_data := alu.io.result
  }

  when(io.dmem.memread || io.dmem.memwrite) {
    io.dmem.valid := true.B
  } .otherwise {
    io.dmem.valid := false.B
  }


  //alu-connections
  alu.io.operation          := aluControl.io.operation
  alu.io.inputx             := registers.io.readdata1
  alu.io.inputy             := registers.io.readdata2


  //aluControl
  aluControl.io.funct7       := instruction(31,25)
  aluControl.io.funct3       := instruction(14,12)
  aluControl.io.immediate    := control.io.immediate
  aluControl.io.add          := control.io.add

  //RegisterFile
  registers.io.readreg1     := instruction(19,15)
  registers.io.readreg2     := instruction(24,20)
  registers.io.writereg     := instruction(11,7)
  registers.io.wen          := control.io.regwrite
  registers.io.writedata    := alu.io.result

  //add instruction

  //control unit
  control.io.opcode          :=  instruction
  //CSR unit
  csr.io.inst                 := instruction
  csr.io.read_data            := registers.io.readdata1
  csr.io.pc                   := pc
  csr.io.illegal_inst         := !control.io.validinst || csr.io.read_illegal || csr.io.write_illegal || csr.io.system_illegal
  csr.io.retire_inst          := true.B
  csr.io.immid                := immGen.io.sextImm

  //plplusfou
  //branchAdd
  //branchAdd.io.inputx          := instruction
  //branchAdd.io.inputy          := pc

  // Debug / pipeline viewer
  val structures = List(
    (control, "control"),
    (registers, "registers"),
    (aluControl, "aluControl"),
    (alu, "alu"),
    (immGen, "immGen"),
    (branchCtrl, "branchCtrl"),
    (pcPlusFour, "pcPlusFour"),
    (branchAdd, "branchAdd")
  )

  printf("DASM(%x)\n", instruction)
  printf(p"CYCLE=$cycleCount\n")
  printf(p"pc: $pc\n")
  for (structure <- structures) {
    printf(p"${structure._2}: ${structure._1.io}\n")
  }
  printf("\n")

}
/*
 * Object to make it easier to print information about the CPU
 */
object SingleCycleCPUInfo {
  def getModules(): List[String] = {
    List(
      "dmem",
      "imem",
      "control",
      "registers",
      "csr",
      "aluControl",
      "alu",
      "immGen",
      "branchCtrl",
      "pcPlusFour",
      "branchAdd"
    )
  }
}
