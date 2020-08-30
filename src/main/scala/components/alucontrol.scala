// This file contains ALU control logic.

package dinocpu.components

import chisel3._
import chisel3.util._

/**
 * The ALU control unit
 *
 * Input:  add, if true, add no matter what the other bits are
 * Input:  immediate, if true, ignore funct7 when computing the operation
 * Input:  funct7, the most significant bits of the instruction
 * Input:  funct3, the middle three bits of the instruction (12-14)
 * Output: operation, What we want the ALU to do.
 *
 * For more information, see Section 4.4 and A.5 of Patterson and Hennessy.
 * This is loosely based on figure 4.12
 */
class ALUControl extends Module {
  val io = IO(new Bundle {
    val add       = Input(Bool())
    val immediate = Input(Bool())
    val funct7    = Input(UInt(7.W))
    val funct3    = Input(UInt(3.W))

    val operation = Output(UInt(4.W))
  })


  when (io.funct7 === 0.U && io.funct3 === 0.U){
    io.operation := 2.U
  }.elsewhen (io.funct7 === 32.U && io.funct3 === 0.U){
    io.operation := 3.U
  }.elsewhen (io.funct7 === 0.U && io.funct3 === 1.U){
    //SLL
    io.operation := 6.U
  }.elsewhen (io.funct7 === 0.U && io.funct3 === 2.U) {
    //SLT
    io.operation := 4.U
  }.elsewhen (io.funct7 === 0.U && io.funct3 === 3.U) {
    //SLTU
    io.operation := 5.U
  }.elsewhen (io.funct7 === 0.U && io.funct3 === 4.U) {
    //XOR
    io.operation := 9.U
  }.elsewhen (io.funct7 === 0.U && io.funct3 === 5.U) {
    //SRL
    io.operation := 7.U
  }.elsewhen (io.funct7 === 32.U && io.funct3 === 5.U) {
    //SRA
    io.operation := 8.U
  }.elsewhen (io.funct7 === 0.U && io.funct3 === 6.U) {
    //OR
    io.operation := 1.U
  }.elsewhen (io.funct7 === 0.U && io.funct3 === 7.U) {
    //AND
    io.operation := 0.U
  }.otherwise {
    io.operation := 15.U // invalid operation
  }
}
