package com.deltax.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.deltax.util.CPoolReader.C_Class;
import com.deltax.util.CPoolReader.C_FMIRefBase;
import com.deltax.util.CPoolReader.C_NameAndType;
import com.deltax.util.CPoolReader.C_Utf8;
import com.deltax.util.CPoolReader.classfile;

/**
 * This class analyzes a bytecode block extracted from  a {@link com.deltax.util.CPoolReader constant pool reader}
 * object.
 * 
 *
 * @author Cristiano Sadun
 */
public class ByteCodeAnalyzer {
    
    public static final int INVOKE_VIRTUAL = 1;
    public static final int INVOKE_STATIC = 2;
    public static final int INVOKE_SPECIAL = 4;
    
    public static final int NOP = 0;
    public static final int ACONST_NULL = 1;
    public static final int ICONST_M1 = 2;
    public static final int ICONST_0 = 3;
    public static final int ICONST_1 = 4;
    public static final int ICONST_2 = 5;
    public static final int ICONST_3 = 6;
    public static final int ICONST_4 = 7;
    public static final int ICONST_5 = 8;
    public static final int LCONST_0 = 9;
    public static final int LCONST_1 = 10;
    public static final int FCONST_0 = 11;
    public static final int FCONST_1 = 12;
    public static final int FCONST_2 = 13;
    public static final int DCONST_0 = 14;
    public static final int DCONST_1 = 15;
    public static final int BIPUSH = 16;
    public static final int SIPUSH = 17;
    public static final int LDC = 18;
    public static final int LDC_W = 19;
    public static final int LDC2_W = 20;
    public static final int ILOAD = 21;
    public static final int LLOAD = 22;
    public static final int FLOAD = 23;
    public static final int DLOAD = 24;
    public static final int ALOAD = 25;
    public static final int ILOAD_0 = 26;
    public static final int ILOAD_1 = 27;
    public static final int ILOAD_2 = 28;
    public static final int ILOAD_3 = 29;
    public static final int LLOAD_0 = 30;
    public static final int LLOAD_1 = 31;
    public static final int LLOAD_2 = 32;
    public static final int LLOAD_3 = 33;
    public static final int FLOAD_0 = 34;
    public static final int FLOAD_1 = 35;
    public static final int FLOAD_2 = 36;
    public static final int FLOAD_3 = 37;
    public static final int DLOAD_0 = 38;
    public static final int DLOAD_1 = 39;
    public static final int DLOAD_2 = 40;
    public static final int DLOAD_3 = 41;
    public static final int ALOAD_0 = 42;
    public static final int ALOAD_1 = 43;
    public static final int ALOAD_2 = 44;
    public static final int ALOAD_3 = 45;
    public static final int IALOAD = 46;
    public static final int LALOAD = 47;
    public static final int FALOAD = 48;
    public static final int DALOAD = 49;
    public static final int AALOAD = 50;
    public static final int BALOAD = 51;
    public static final int CALOAD = 52;
    public static final int SALOAD = 53;
    public static final int ISTORE = 54;
    public static final int LSTORE = 55;
    public static final int FSTORE = 56;
    public static final int DSTORE = 57;
    public static final int ASTORE = 58;
    public static final int ISTORE_0 = 59;
    public static final int ISTORE_1 = 60;
    public static final int ISTORE_2 = 61;
    public static final int ISTORE_3 = 62;
    public static final int LSTORE_0 = 63;
    public static final int LSTORE_1 = 64;
    public static final int LSTORE_2 = 65;
    public static final int LSTORE_3 = 66;
    public static final int FSTORE_0 = 67;
    public static final int FSTORE_1 = 68;
    public static final int FSTORE_2 = 69;
    public static final int FSTORE_3 = 70;
    public static final int DSTORE_0 = 71;
    public static final int DSTORE_1 = 72;
    public static final int DSTORE_2 = 73;
    public static final int DSTORE_3 = 74;
    public static final int ASTORE_0 = 75;
    public static final int ASTORE_1 = 76;
    public static final int ASTORE_2 = 77;
    public static final int ASTORE_3 = 78;
    public static final int IASTORE = 79;
    public static final int LASTORE = 80;
    public static final int FASTORE = 81;
    public static final int DASTORE = 82;
    public static final int AASTORE = 83;
    public static final int BASTORE = 84;
    public static final int CASTORE = 85;
    public static final int SASTORE = 86;
    public static final int POP = 87;
    public static final int POP2 = 88;
    public static final int DUP = 89;
    public static final int DUP_X1 = 90;
    public static final int DUP_X2 = 91;
    public static final int DUP2 = 92;
    public static final int DUP2_X1 = 93;
    public static final int DUP2_X2 = 94;
    public static final int SWAP = 95;
    public static final int IADD = 96;
    public static final int LADD = 97;
    public static final int FADD = 98;
    public static final int DADD = 99;
    public static final int ISUB = 100;
    public static final int LSUB = 101;
    public static final int FSUB = 102;
    public static final int DSUB = 103;
    public static final int IMUL = 104;
    public static final int LMUL = 105;
    public static final int FMUL = 106;
    public static final int DMUL = 107;
    public static final int IDIV = 108;
    public static final int LDIV = 109;
    public static final int FDIV = 110;
    public static final int DDIV = 111;
    public static final int IREM = 112;
    public static final int LREM = 113;
    public static final int FREM = 114;
    public static final int DREM = 115;
    public static final int INEG = 116;
    public static final int LNEG = 117;
    public static final int FNEG = 118;
    public static final int DNEG = 119;
    public static final int ISHL = 120;
    public static final int LSHL = 121;
    public static final int ISHR = 122;
    public static final int LSHR = 123;
    public static final int IUSHR = 124;
    public static final int LUSHR = 125;
    public static final int IAND = 126;
    public static final int LAND = 127;
    public static final int IOR = 128;
    public static final int LOR = 129;
    public static final int IXOR = 130;
    public static final int LXOR = 131;
    public static final int IINC = 132;
    public static final int I2L = 133;
    public static final int I2F = 134;
    public static final int I2D = 135;
    public static final int L2I = 136;
    public static final int L2F = 137;
    public static final int L2D = 138;
    public static final int F2I = 139;
    public static final int F2L = 140;
    public static final int F2D = 141;
    public static final int D2I = 142;
    public static final int D2L = 143;
    public static final int D2F = 144;
    //public static final int INT2BYTE = 145;
    //public static final int INT2CHAR = 146;
    //public static final int INT2SHORT = 147;
    public static final int LCMP = 148;
    public static final int FCMPL = 149;
    public static final int FCMPG = 150;
    public static final int DCMPL = 151;
    public static final int DCMPG = 152;
    public static final int IFEQ = 153;
    public static final int IFNE = 154;
    public static final int IFLT = 155;
    public static final int IFGE = 156;
    public static final int IFGT = 157;
    public static final int IFLE = 158;
    public static final int IF_ICMPEQ = 159;
    public static final int IF_ICMPNE = 160;
    public static final int IF_ICMPLT = 161;
    public static final int IF_ICMPGE = 162;
    public static final int IF_ICMPGT = 163;
    public static final int IF_ICMPLE = 164;
    public static final int IF_ACMPEQ = 165;
    public static final int IF_ACMPNE = 166;
    public static final int GOTO = 167;
    public static final int JSR = 168;
    public static final int RET = 169;
    public static final int TABLESWITCH = 170;
    public static final int LOOKUPSWITCH = 171;
    public static final int IRETURN = 172;
    public static final int LRETURN = 173;
    public static final int FRETURN = 174;
    public static final int DRETURN = 175;
    public static final int ARETURN = 176;
    public static final int RETURN = 177;
    public static final int GETSTATIC = 178;
    public static final int PUTSTATIC = 179;
    public static final int GETFIELD = 180;
    public static final int PUTFIELD = 181;
    public static final int INVOKEVIRTUAL = 182;
    public static final int INVOKESPECIAL = 183;
    public static final int INVOKESTATIC = 184;
    public static final int INVOKEINTERFACE = 185;
   
    public static final int NEW = 187;
    public static final int NEWARRAY = 188;
    public static final int ANEWARRAY = 189;
    public static final int ARRAYLENGTH = 190;
    public static final int ATHROW = 191;
    public static final int CHECKCAST = 192;
    public static final int INSTANCEOF = 193;
    public static final int MONITORENTER = 194;
    public static final int MONITOREXIT = 195;
    public static final int WIDE = 196;
    public static final int MULTIANEWARRAY = 197;
    public static final int IFNULL = 198;
    public static final int IFNONNULL = 199;
    public static final int GOTO_W = 200;
    public static final int JSR_W = 201;
    
    private static int[] instructions;
    private static String [] names; 
    private byte[] byteCode;
    private classfile cf;
    private boolean storeStringSignatures;
    private String codeBlockName;
    
    private static final boolean debug = false;
    
    public ByteCodeAnalyzer(classfile cf, String codeBlockName, byte[] byteCode) {
        this.byteCode=byteCode;
        this.cf=cf;
        this.codeBlockName=codeBlockName;
    }

    /**
     * This method produces a Map containing class names as keys, and sets of method signatures (Strings)
     * as entries, where the classes and methods are actually used in the bytecode block.
     * 
     * @param invocationTypes one of {@link #INVOKE_VIRTUAL},{@link #INVOKE_STATIC} or {@link #INVOKE_SPECIAL}.
     * @return a Map containing class names as keys, and sets of method signatures (Strings)
     * as entries.
     */
    public Map findInvokedClassesAndMethods(int invocationTypes, boolean externalize) {
        Map signaturesMap=new HashMap();
        
        if (debug) {
        System.out.println();
        System.out.println(codeBlockName+":");
        }
        
        for(int i=0;i<byteCode.length;) {
            int opCode=byteCode[i];
            if (opCode<0) opCode=256+opCode;
            int pLen=instructions[opCode];
            if (pLen==-1) pLen=handleSpecial(opCode, i);
            if (opCode==INVOKEVIRTUAL && (invocationTypes & INVOKE_VIRTUAL)!=0
                || opCode==INVOKESTATIC && (invocationTypes & INVOKE_STATIC)!=0
                || opCode==INVOKESPECIAL && (invocationTypes & INVOKE_SPECIAL)!=0
                ) {
                if (debug) {
                    String name = names[opCode];
                    System.out.print(name+" ");
                }
                int cpIndex = mkCPIndex(byteCode, i+1);
                addInvocationDataToMap(opCode, externalize, signaturesMap, cpIndex);
            } else if (opCode == INVOKEINTERFACE) {
                int cpIndex = mkCPIndex(byteCode, i+1);
                addInvocationDataToMap(opCode, externalize, signaturesMap, cpIndex);
            }
            i+=1+pLen;
        }
        
        return signaturesMap;
    }

    /**
     * @param opCode 
     * @param externalize
     * @param signaturesMap
     * @param cpIndex
     * @param pw 
     */
    private void addInvocationDataToMap(int opCode, boolean externalize, Map signaturesMap, int cpIndex) {
        
        try {
            
            if (debug) {
                System.out.print(cpIndex+" - ");
            }
        
            C_FMIRefBase methodref = (C_FMIRefBase)cf.constant_pool[cpIndex];
            
            C_Class cls = (C_Class)cf.constant_pool[methodref.class_index];
            C_Utf8 icls=(C_Utf8)cf.constant_pool[cls.name_index];
            
            C_NameAndType nt = (C_NameAndType)cf.constant_pool[methodref.name_and_type_index];
            
            C_Utf8 mName = (C_Utf8)cf.constant_pool[nt.name_index];
            C_Utf8 mDesc = (C_Utf8)cf.constant_pool[nt.descriptor_index];
            
            int z = mDesc.getBytesAsString().indexOf(")");
            String paramsInternal=mDesc.getBytesAsString().substring(1, z);
            String retTypeInternal=mDesc.getBytesAsString().substring(z+1);
            
            String clsName=(externalize ? externalizeName(icls.getBytesAsString()) : icls.getBytesAsString());
            
            StringBuffer sb=null;
            MethodData md=null;
            
            if (debug) {
                System.out.println(mName.getBytesAsString());
            }
            
            
            if (storeStringSignatures) {
                sb = new StringBuffer();
                if (externalize) {
                    sb.append(SignatureAnalyzer.getJavaTypeName(retTypeInternal));
                    sb.append(" ");
                    sb.append(clsName);
                    sb.append(".");
                    sb.append(mName.getBytesAsString());
                    sb.append("(");
                    sb.append(SignatureAnalyzer.toExternalParamsList(paramsInternal));
                    sb.append(")");
                } else {
                    sb.append(clsName);
                    sb.append(" ");
                    sb.append(mName.getBytesAsString());
                    sb.append(" ");
                    sb.append(mDesc.getBytesAsString());
                }
            } else {
                md = new MethodData(externalizeName(icls.getBytesAsString()), 
                                    mName.getBytesAsString(), 
                                    SignatureAnalyzer.getJavaTypeName(retTypeInternal),
                                    SignatureAnalyzer.toExternalParamsList(paramsInternal));
            }
            
            if (clsName==null) 
                throw new RuntimeException("ClsName null!");
            Set s = (Set)signaturesMap.get(clsName);
            if (s==null) 
                signaturesMap.put(clsName, s=new HashSet());
         
            if (storeStringSignatures) 
                s.add(sb.toString());
            else  {
                if (md==null) throw new RuntimeException("Internal error: unexpected null value for method data");
                s.add(md);
            }
        } catch(RuntimeException e) {
            System.err.println();
            if (e instanceof ClassCastException)
                System.err.println("Unexpected entry in constant pool at position "+cpIndex+": "+e.getMessage()+"! (interpreting "+names[opCode]+" "+cpIndex+") in code block "+codeBlockName);
            else
            System.err.println("Exception "+e.getClass().getName()+" ("+e.getMessage()+") interpreting "+names[opCode]+" "+cpIndex+" in code block "+codeBlockName);
            System.err.println();
            
            cf.dumpConstantPool(System.err);
            throw e;
        }
    }
    
    private int mkCPIndex(byte [] data, int pos) {
        return mkCPIndex(data[pos], data[pos+1]);
    }
    
    private int mkCPIndex(byte _indexbyte1, byte _indexbyte2) {
        
        int indexbyte1=_indexbyte1;
        int indexbyte2=_indexbyte2;
        
        if (_indexbyte2 < 0) {
            indexbyte2=256+indexbyte2;  
            int z2=((indexbyte1 << 8) + indexbyte2);
            return z2;
        } 
        
        int z=((indexbyte1 << 8) + (indexbyte2 << 0));
        if (z<0) {
            return 256+z;
        }
        return z;
    }
    
    private String externalizeName(String cls) {
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<cls.length();i++) {
            char c= cls.charAt(i);
            if (c=='/') sb.append('.');
            else sb.append(c);
        }
        return sb.toString();
    }
    
    private int handleSpecial(int opCode, int i) {
        switch(opCode) {
            case LOOKUPSWITCH:
                throw new RuntimeException("opcode LOOKUPSWITCH handling not implemented yet ("+opCode+")");
                //break;
            case TABLESWITCH:
                throw new RuntimeException("opcode TABLESWITCH handling not implemented yet ("+opCode+")");
                //break;
            case WIDE:
                throw new RuntimeException("opcode WIDE handling not implemented yet ("+opCode+")");
                //break;
            default:
                throw new RuntimeException("Disassembly problem: unexpected special handling for opCode "+opCode); 
        }
    }

    static {
        final int [][] operands = {
                { AALOAD, 0 },
                { AASTORE,  0 },
                { ACONST_NULL, 0 },
                { ALOAD,  1},
                { ALOAD_0, 0 },
                { ALOAD_1, 0 },
                { ALOAD_2, 0 },
                { ALOAD_3, 0 },
                { ANEWARRAY, 2 },
                { ARETURN,  0 },
                { ARRAYLENGTH, 0 },
                { ASTORE, 1 },
                { ASTORE_0, 0 },
                { ASTORE_1, 0 },
                { ASTORE_2, 0 },
                { ASTORE_3, 0 },
                { ATHROW, 0 },
                { BALOAD, 0 },
                { BASTORE, 0 },
                { BIPUSH, 1 },
                { CALOAD, 0 },
                { CASTORE, 0 },
                { CHECKCAST, 2 },
                { D2F, 0 },
                { D2I, 0 },
                { D2L, 0 },
                { DADD, 0 },
                { DALOAD, 0 },
                { DASTORE, 0 },
                { DCMPG, 0 },
                { DCMPL, 0 },
                { DCONST_0, 0 },
                { DCONST_1, 0 },
                { DDIV, 0 },
                { DLOAD, 1 },
                { DLOAD_0, 0 },
                { DLOAD_1, 0 },
                { DLOAD_2, 0 },
                { DLOAD_3, 0 },
                { DMUL, 0 },
                { DNEG, 0 },
                { DREM, 0 },
                { DRETURN, 0 },
                { DSTORE, 1 },
                { DSTORE_0, 0 },
                { DSTORE_1, 0 },
                { DSTORE_2, 0 },
                { DSTORE_3, 0 },
                { DSUB, 0 },
                { DUP, 0 },
                { DUP_X1, 0 },
                { DUP_X2, 0 },
                { DUP2, 0 },
                { DUP2_X1, 0 },
                { DUP2_X2, 0 },
                { F2D, 0 },
                { F2I, 0 },
                { F2L, 0 },
                { FADD, 0 },
                { FALOAD, 0 },
                { FASTORE, 0 },
                { FCMPG, 0 },
                { FCMPL, 0 },
                { FCONST_0, 0 },
                { FCONST_1, 0 },
                { FCONST_2, 0 },
                { FDIV, 0 },
                { FLOAD, 1 },
                { FLOAD_0, 0 },
                { FLOAD_1, 0 },
                { FLOAD_2, 0 },
                { FLOAD_3, 0 },
                { FMUL, 0 },
                { FNEG, 0 },
                { FREM, 0 },
                { FRETURN, 0 },
                { FSTORE, 1 },
                { FSTORE_0, 0 },
                { FSTORE_1, 0 },
                { FSTORE_2, 0 },
                { FSTORE_3, 0 },
                { FSUB, 0 },
                { GETFIELD, 2 },
                { GETSTATIC, 2 },
                { GOTO, 2 },
                { GOTO_W, 4 },
                { I2D, 0 },
                { I2F, 0 },
                { I2L, 0 },
                { IADD, 0 },
                { IALOAD, 0 },
                { IAND, 0 },
                { IASTORE, 0 },
                { ICONST_0, 0 },
                { ICONST_1, 0 },
                { ICONST_2, 0 },
                { ICONST_3, 0 },
                { ICONST_4, 0 },
                { ICONST_5, 0 },
                { ICONST_M1, 0 },
                { IDIV, 0 },
                { IF_ACMPEQ, 2 },
                { IF_ACMPNE, 2 },
                { IF_ICMPEQ, 2 },
                { IF_ICMPGE, 2 },
                { IF_ICMPGT, 2 },
                { IF_ICMPLE, 2 },
                { IF_ICMPLT, 2 },
                { IF_ICMPNE, 2 },
                { IFEQ, 2 },
                { IFGE, 2 },
                { IFGT, 2 },
                { IFLE, 2 },
                { IFLT, 2 },
                { IFNE, 2 },
                { IFNONNULL, 2 },
                { IFNULL, 2 },
                { IINC, 2 },
                { ILOAD, 1 },
                { ILOAD_0, 0 },
                { ILOAD_1, 0 },
                { ILOAD_2, 0 },
                { ILOAD_3, 0 },
                { IMUL, 0 },
                { INEG, 0 },
                { INSTANCEOF, 2 },
                { INVOKEINTERFACE,4  },
                { INVOKESPECIAL, 2 },
                { INVOKESTATIC, 2 },
                { INVOKEVIRTUAL, 2 },
                { IOR, 0 },
                { IREM, 0 },
                { IRETURN, 0 },
                { ISHL, 0 },
                { ISHR, 0 },
                { ISTORE, 1 },
                { ISTORE_0, 0 },
                { ISTORE_1, 0 },
                { ISTORE_2, 0 },
                { ISTORE_3, 0 },
                { ISUB, 0 },
                { IUSHR, 0 },
                { IXOR, 0 },
                { JSR, 2 },
                { JSR_W , 4 },
                { L2D, 0 },
                { L2F, 0 },
                { L2I, 0 },
                { LADD, 0 },
                { LALOAD, 0 },
                { LAND, 0 },
                { LASTORE, 0 },
                { LCMP, 0 },
                { LCONST_0, 0 },
                { LCONST_1, 0 },
                { LDC, 1 },
                { LDC_W, 2 },
                { LDC2_W, 2 },
                { LDIV, 0 },
                { LLOAD, 1 },
                { LLOAD_0, 0 },
                { LLOAD_1, 0 },
                { LLOAD_2, 0 },
                { LLOAD_3, 0 },
                { LMUL, 0 },
                { LNEG, 0 },
                { LOOKUPSWITCH, -1 }, // Special handling
                { LOR, 0 },
                { LREM, 0 },
                { LRETURN, 0 },
                { LSHL, 0 },
                { LSHR, 0 },
                { LSTORE, 1 },
                { LSTORE_0, 0 },
                { LSTORE_1, 0 },
                { LSTORE_2, 0 },
                { LSTORE_3, 0 },
                { LSUB, 0 },
                { LUSHR, 0 },
                { LXOR, 0 },
                { MONITORENTER, 0 },
                { MONITOREXIT, 0 },
                { MULTIANEWARRAY, 3 },
                { NEW, 2 },
                { NEWARRAY, 1 },
                { NOP, 0 },
                { POP, 0 },
                { POP2, 0 },
                { PUTFIELD, 2 },
                { PUTSTATIC, 2 },
                { RET, 1 },
                { RETURN, 0 },
                { SALOAD, 0 },
                { SASTORE, 0 },
                { SIPUSH, 2 },
                { SWAP, 0 },
                { TABLESWITCH, -1 }, // Special handling
                { WIDE, -1 }, // Special handling
            };
        
        final String [] nn = {
                "AALOAD",
                "AASTORE",
                "ACONST_NULL",
                "ALOAD",
                "ALOAD_0",
                "ALOAD_1",
                "ALOAD_2",
                "ALOAD_3",
                "ANEWARRAY",
                "ARETURN",
                "ARRAYLENGTH",
                "ASTORE",
                "ASTORE_0",
                "ASTORE_1",
                "ASTORE_2",
                "ASTORE_3",
                "ATHROW",
                "BALOAD",
                "BASTORE",
                "BIPUSH",
                "CALOAD",
                "CASTORE",
                "CHECKCAST",
                "D2F",
                "D2I",
                "D2L",
                "DADD",
                "DALOAD",
                "DASTORE",
                "DCMPG",
                "DCMPL",
                "DCONST_0",
                "DCONST_1",
                "DDIV",
                "DLOAD",
                "DLOAD_0",
                "DLOAD_1",
                "DLOAD_2",
                "DLOAD_3",
                "DMUL",
                "DNEG",
                "DREM",
                "DRETURN",
                "DSTORE",
                "DSTORE_0",
                "DSTORE_1",
                "DSTORE_2",
                "DSTORE_3",
                "DSUB",
                "DUP",
                "DUP_X1",
                "DUP_X2",
                "DUP2",
                "DUP2_X1",
                "DUP2_X2",
                "F2D",
                "F2I",
                "F2L",
                "FADD",
                "FALOAD",
                "FASTORE",
                "FCMPG",
                "FCMPL",
                "FCONST_0",
                "FCONST_1",
                "FCONST_2",
                "FDIV",
                "FLOAD",
                "FLOAD_0",
                "FLOAD_1",
                "FLOAD_2",
                "FLOAD_3",
                "FMUL",
                "FNEG",
                "FREM",
                "FRETURN",
                "FSTORE",
                "FSTORE_0",
                "FSTORE_1",
                "FSTORE_2",
                "FSTORE_3",
                "FSUB",
                "GETFIELD",
                "GETSTATIC",
                "GOTO",
                "GOTO_W",
                "I2D",
                "I2F",
                "I2L",
                "IADD",
                "IALOAD",
                "IAND",
                "IASTORE",
                "ICONST_0",
                "ICONST_1",
                "ICONST_2",
                "ICONST_3",
                "ICONST_4",
                "ICONST_5",
                "ICONST_M1",
                "IDIV",
                "IF_ACMPEQ",
                "IF_ACMPNE",
                "IF_ICMPEQ",
                "IF_ICMPGE",
                "IF_ICMPGT",
                "IF_ICMPLE",
                "IF_ICMPLT",
                "IF_ICMPNE",
                "IFEQ",
                "IFGE",
                "IFGT",
                "IFLE",
                "IFLT",
                "IFNE",
                "IFNONNULL",
                "IFNULL",
                "IINC",
                "ILOAD",
                "ILOAD_0",
                "ILOAD_1",
                "ILOAD_2",
                "ILOAD_3",
                "IMUL",
                "INEG",
                "INSTANCEOF",
                "INVOKEINTERFACE,4",
                "INVOKESPECIAL",
                "INVOKESTATIC",
                "INVOKEVIRTUAL",
                "IOR",
                "IREM",
                "IRETURN",
                "ISHL",
                "ISHR",
                "ISTORE",
                "ISTORE_0",
                "ISTORE_1",
                "ISTORE_2",
                "ISTORE_3",
                "ISUB",
                "IUSHR",
                "IXOR",
                "JSR",
                "JSR_W",
                "L2D",
                "L2F",
                "L2I",
                "LADD",
                "LALOAD",
                "LAND",
                "LASTORE",
                "LCMP",
                "LCONST_0",
                "LCONST_1",
                "LDC",
                "LDC_W",
                "LDC2_W",
                "LDIV",
                "LLOAD",
                "LLOAD_0",
                "LLOAD_1",
                "LLOAD_2",
                "LLOAD_3",
                "LMUL",
                "LNEG",
                "LOOKUPSWITCH, -1", 
                "LOR",
                "LREM",
                "LRETURN",
                "LSHL",
                "LSHR",
                "LSTORE",
                "LSTORE_0",
                "LSTORE_1",
                "LSTORE_2",
                "LSTORE_3",
                "LSUB",
                "LUSHR",
                "LXOR",
                "MONITORENTER",
                "MONITOREXIT",
                "MULTIANEWARRAY",
                "NEW",
                "NEWARRAY",
                "NOP",
                "POP",
                "POP2",
                "PUTFIELD",
                "PUTSTATIC",
                "RET",
                "RETURN",
                "SALOAD",
                "SASTORE",
                "SIPUSH",
                "SWAP",
                "TABLESWITCH, -1", 
                "WIDE, -1", 
        };
        
        instructions=new int[210];
        names=new String[instructions.length];
        
        for(int i=0;i<operands.length;i++) {
            int op=operands[i][0];
            int pLen=operands[i][1];
            instructions[op]=pLen;
            names[op]=nn[i];
        }
    }
    
    /**
     * If true, the map returned by {@link #findInvokedClassesAndMethods(int, boolean)} will
     * contain sets of Strings. If false, it will contain Sets of {@link MethodData} objects.
     */
    public boolean isStoreStringSignatures() {
        return storeStringSignatures;
    }

    /**
     * If set to true, the map returned by {@link #findInvokedClassesAndMethods(int, boolean)} will
     * contain sets of Strings. If false, it will contain Sets of {@link MethodData} objects.
     * 
     * @param storeStringSignatures
     */
    public void setStoreStringSignatures(boolean storeStringSignatures) {
        this.storeStringSignatures = storeStringSignatures;
    }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        classfile cf = new CPoolReader("C:\\netcom2_tibco\\java\\ih\\BillingSystemHandler_iCIM3.0\\dist\\NinjaiCIM.jar").readClass("no.netcom.ipl.ih.billingsystem.smodelninjatranslators.producthandling.StandardMappingRoutines");
        Map map = cf.getUsedMethods();
        System.out.println(map.keySet().size()+" classes used");
        for(Iterator i = map.keySet().iterator();i.hasNext();) {
            String cls = (String)i.next();
            Set methods = (Set)map.get(cls); 
            for(Iterator j = methods.iterator();j.hasNext();) {
                MethodData method = (MethodData)j.next();
                System.out.println(cls+" uses:\t"+method);
            }
        }
    }

    public String getCodeBlockName() {
        return codeBlockName;
    }

}
