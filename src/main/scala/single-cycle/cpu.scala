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
  control.io    := DontCare
  immGen.io     := DontCare
  branchCtrl.io := DontCare
  pcPlusFour.io := DontCare
  branchAdd.io  := DontCare

  io.imem.address := pc

  val instruction = io.imem.instruction

  //alu-connections
  alu.io.operation          := aluControl.io.operation
  alu.io.inputx             := registers.io.readdata1
  alu.io.inputy             := registers.io.readdata2


  //aluControl
  aluControl.io.funct7       := instruction(31,25)
  aluControl.io.funct3       := instruction(14,12)

  //RegisterFile
  registers.io.readreg1     := instruction(19,15)
  registers.io.readreg2     := instruction(24,20)
  registers.io.writereg     := instruction(11,7)
  registers.io.wen          := 1.U
  registers.io.writedata    := alu.io.result

  //add instruction


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
