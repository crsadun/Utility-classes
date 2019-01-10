/************************************************************************************
* Copyright (C) 1999 Cristiano Sadun
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
************************************************************************************/

package com.deltax.util;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * This class reads directly a class file as specified in the Java Language
 * specification. <br>Unfortunately java.lang.ClassLoader does not provide
 * services for accessing class bytecode, so a ClassFileFinder object is used
 * to locate and load the bytecode for a class. <br>Generally this scheme must
 * be identical to the currently used ClassLoader scheme. <br>A JDK 1.2
 * ClassFileFinder - which emulates standard JDK 1.2 behaviour is used by
 * default, but a different ClassFileFinder can be specified at construction
 * time.
 * 
 * @see JDK12ClassFileFinder
 * 
 * @author Cristiano Sadun
 * @version 1.1
 */
public class CPoolReader {

	private static final String lineSep =
		System.getProperties().getProperty("line.separator");

	private static boolean debug = false;

	public static final byte CONSTANT_Class = 7;
	public static final byte CONSTANT_Fieldref = 9;
	public static final byte CONSTANT_Methodref = 10;
	public static final byte CONSTANT_InterfaceMethodref = 11;
	public static final byte CONSTANT_String = 8;
	public static final byte CONSTANT_Integer = 3;
	public static final byte CONSTANT_Float = 4;
	public static final byte CONSTANT_Long = 5;
	public static final byte CONSTANT_Double = 6;
	public static final byte CONSTANT_NameAndType = 12;
	public static final byte CONSTANT_Utf8 = 1;
	public static final byte CONSTANT_Unknown = 127;

	public static final String CONSTANT_Class_s = "CONSTANT_Class";
	public static final String CONSTANT_Fieldref_s = "CONSTANT_Fieldref";
	public static final String CONSTANT_Methodref_s = "CONSTANT_Methodref";
	public static final String CONSTANT_InterfaceMethodref_s =
		"CONSTANT_InterfaceMethodref";
	public static final String CONSTANT_String_s = "CONSTANT_String";
	public static final String CONSTANT_Integer_s = "CONSTANT_Integer";
	public static final String CONSTANT_Float_s = "CONSTANT_Float";
	public static final String CONSTANT_Long_s = "CONSTANT_Long";
	public static final String CONSTANT_Double_s = "CONSTANT_Double";
	public static final String CONSTANT_NamedType_s = "CONSTANT_NameAndType";
	public static final String CONSTANT_Utf8_s = "CONSTANT_Utf8";
	public static final String CONSTANT_Unknown_s = "unknown";

	static final String[] c_names = { CONSTANT_Utf8_s, // 1
		CONSTANT_Unknown_s, // 2
		CONSTANT_Integer_s, // 3
		CONSTANT_Float_s, // 4
		CONSTANT_Long_s, // 5
		CONSTANT_Double_s, // 6
		CONSTANT_Class_s, // 7
		CONSTANT_String_s, // 8
		CONSTANT_Fieldref_s, // 9
		CONSTANT_Methodref_s, // 10
		CONSTANT_InterfaceMethodref_s, // 11
		CONSTANT_NamedType_s // 12
	};

	public static String getConstTag_s(short tag) {
		if (tag > c_names.length)
			return CONSTANT_Unknown_s;
		return c_names[(int) (tag - 1)];
	}

	public static final short ACC_PUBLIC = 0x0001;
	public static final short ACC_PRIVATE = 0x0002;
	public static final short ACC_PROTECTED = 0x0004;
	public static final short ACC_STATIC = 0x0008;
	public static final short ACC_FINAL = 0x0010;
	public static final short ACC_VOLATILE = 0x0040;
	public static final short ACC_TRANSIENT = 0x0080;
	public static final short ACC_SYNCHRONIZED = 0x0020;
	public static final short ACC_NATIVE = 0x0100;
	public static final short ACC_ABSTRACT = 0x0400;
	public static final short ACC_INTERFACE = 0x0200;

	public static final String ACC_PUBLIC_s = "PUBLIC";
	public static final String ACC_PRIVATE_s = "PRIVATE";
	public static final String ACC_PROTECTED_s = "PROTECTED";
	public static final String ACC_STATIC_s = "STATIC";
	public static final String ACC_FINAL_s = "FINAL";
	public static final String ACC_VOLATILE_s = "VOLATILE";
	public static final String ACC_TRANSIENT_s = "TRANSIENT";
	public static final String ACC_SYNCHRONIZED_s = "SYNCHRONIZED";
	public static final String ACC_NATIVE_s = "NATIVE";
	public static final String ACC_ABSTRACT_s = "ABSTRACT";
	public static final String ACC_INTERFACE_s = "INTERFACE";
    

	public static String getAcc_s(short access_flag) {
		StringBuffer sb = new StringBuffer();
		synchronized (sb) {
			if ((access_flag & ACC_PUBLIC) != 0)
				sb.append(ACC_PUBLIC_s + "  ");
			if ((access_flag & ACC_PRIVATE) != 0)
				sb.append(ACC_PRIVATE_s + "  ");
			if ((access_flag & ACC_PROTECTED) != 0)
				sb.append(ACC_PROTECTED_s + "  ");
			if ((access_flag & ACC_STATIC) != 0)
				sb.append(ACC_STATIC_s + "  ");
			if ((access_flag & ACC_FINAL) != 0)
				sb.append(ACC_FINAL_s + "  ");
			if ((access_flag & ACC_VOLATILE) != 0)
				sb.append(ACC_VOLATILE_s + "  ");
			if ((access_flag & ACC_TRANSIENT) != 0)
				sb.append(ACC_TRANSIENT_s + "  ");
			if ((access_flag & ACC_SYNCHRONIZED) != 0)
				sb.append(ACC_SYNCHRONIZED_s + "  ");
			if ((access_flag & ACC_NATIVE) != 0)
				sb.append(ACC_NATIVE_s + "  ");
			if ((access_flag & ACC_ABSTRACT) != 0)
				sb.append(ACC_ABSTRACT_s + "  ");
			if ((access_flag & ACC_INTERFACE) != 0)
				sb.append(ACC_INTERFACE_s + "  ");
		}
		return sb.toString().trim();
	}

	/**
	 * The ClassFileFinder object used to locate bytecode By default, a plain
	 * JDK12ClassFileFinder.
	 */
	protected ClassFileFinder cff;

	/**
	 * A cached instance of classfile, used by readClass to speed up things
	 */
	private classfile classfileCache;

	/**
	 * The basic cp_info class.
	 */
	public abstract class cp_info {

		byte tag;

		cp_info(byte tag) {
			this.tag = tag;
		}
		public short getTag() {
			return tag;
		}
		public String getTagName() {
			return getConstTag_s(tag);
		}
		public String toString() {
			return "[Tag = " + tag + " " + getConstTag_s(tag);
		}
	}

	// cp_info substructures

	/**
	 * An helper class used for internal debugging only
	 */
	public class unknown_cp_info extends cp_info {
		unknown_cp_info() {
			super(CONSTANT_Unknown);
		}
		public String toString() {
			return super.toString() + " (UNKNOWN)]";
		}
	}

	public class C_Class extends cp_info {
		short name_index;
		C_Class(DataInput is) throws IOException {
			super(CONSTANT_Class);
			name_index = is.readShort();
		}
		public String toString() {
			return super.toString() + ", name_index = " + name_index + "]";
		}
	}

	/**
	 * Base for CONSTANT_Fieldref, CONSTANT_Methodref,
	 * CONSTANT_InterfaceMethodref entries
	 */
	public abstract class C_FMIRefBase extends cp_info {
		short class_index;
		short name_and_type_index;
		C_FMIRefBase(byte tag, DataInput is) throws IOException {
			super(tag);
			class_index = is.readShort();
			name_and_type_index = is.readShort();
		}
		public String toString() {
			return super.toString()
				+ ", class_index = "
				+ class_index
				+ ", name_and_type_index = "
				+ name_and_type_index
				+ "]";
		}
	}

	public class C_Fieldref extends C_FMIRefBase {
		C_Fieldref(DataInput is) throws IOException {
			super(CONSTANT_Fieldref, is);
		}
	}
	public class C_Methodref extends C_FMIRefBase {
		C_Methodref(DataInput is) throws IOException {
			super(CONSTANT_Methodref, is);
		}
	}
	public class C_InterfaceMethodref extends C_FMIRefBase {
		C_InterfaceMethodref(DataInput is) throws IOException {
			super(CONSTANT_InterfaceMethodref, is);
		}
	}

	public class C_String extends cp_info {
		short string_index;
		C_String(DataInput is) throws IOException {
			super(CONSTANT_String);
			string_index = is.readShort();
		}
		public String toString() {
			return super.toString() + ", string_index = " + string_index + "]";
		}
	}

	public abstract class C_IFBase extends cp_info {
		int bytes;
		C_IFBase(byte tag, DataInput is) throws IOException {
			super(tag);
			bytes = is.readInt();
		}
		public String toString() {
			return super.toString() + ", bytes = " + bytes + "]";
		}
	}

	public class C_Integer extends C_IFBase {
		C_Integer(DataInput is) throws IOException {
			super(CONSTANT_Integer, is);
		}
	}
	public class C_Float extends C_IFBase {
		C_Float(DataInput is) throws IOException {
			super(CONSTANT_Float, is);
		}
	}

	public abstract class C_LDBase extends cp_info {
		int high_bytes;
		int low_bytes;
		C_LDBase(byte tag, DataInput is) throws IOException {
			super(tag);
			high_bytes = is.readInt();
			low_bytes = is.readInt();
		}
		public String toString() {
			return super.toString()
				+ ", high_bytes = "
				+ high_bytes
				+ ", low_bytes = "
				+ low_bytes
				+ "]";
		}
	}

	public class C_Long extends C_LDBase {
		C_Long(DataInput is) throws IOException {
			super(CONSTANT_Long, is);
		}
	}
	public class C_Double extends C_LDBase {
		C_Double(DataInput is) throws IOException {
			super(CONSTANT_Double, is);
		}
	}

	public class C_NameAndType extends cp_info {
		short name_index;
		short descriptor_index;
		C_NameAndType(DataInput is) throws IOException {
			super(CONSTANT_NameAndType);
			name_index = is.readShort();
			descriptor_index = is.readShort();
		}
		public String toString() {
			return super.toString()
				+ ", name_index = "
				+ name_index
				+ ", descriptor_index = "
				+ descriptor_index
				+ "]";
		}
	}

	public class C_Utf8 extends cp_info {
		// byte bytes[]; // byte bytes[lenght];
		String utf;
		C_Utf8(DataInput is) throws IOException {
			super(CONSTANT_Utf8);
			//short length=is.readShort();
			utf = is.readUTF();

			/*
			 * //if (debug) // System.out.println("lenght is "+length);
			 * bytes=new byte[length]; if (debug)
			 * ((DebugInputStream)is).getInputStream().read(bytes); else
			 * ((DataInputStream)is).read(bytes);
			 */
		}
		//public String toString() { return super.toString()+", bytes =
		// \""+new
		// String(bytes)+"\"]"; }
		//public String getBytesAsString() { return new String(bytes); }
		public String toString() {
			return super.toString() + ", utf = \"" + utf + "\"]";
		}
		public String getBytesAsString() {
			return utf;
		}
	}

	public abstract class MF_info {
		short access_flags;
		short name_index;
		short descriptor_index;
		attribute_info attributes[]; //attribute_info
		// attributes[attributes_count];

		MF_info(DataInput is) throws IOException {
			access_flags = is.readShort();
			name_index = is.readShort();
			descriptor_index = is.readShort();

			if (debug)
				System.out.println(
					"access_flags = ("
						+ getAcc_s(access_flags)
						+ "),"
						+ lineSep
						+ "name_index = "
						+ name_index
						+ ","
						+ lineSep
						+ "descriptor_index = "
						+ descriptor_index);

			short attributes_count = is.readShort();
			attributes = new attribute_info[attributes_count];
			for (int i = 0; i < attributes_count; i++)
				attributes[i] = new attribute_info(is);
		}

		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(
				"access_flags = ("
					+ getAcc_s(access_flags)
					+ "),"
					+ lineSep
					+ "name_index = "
					+ name_index
					+ ","
					+ lineSep
					+ "descriptor_index = "
					+ descriptor_index);

			for (int i = 0; i < attributes.length; i++) {
				sb.append(lineSep);
				sb.append("attribute[" + i + "] = ");
				sb.append(attributes[i].toString());
			}

			return sb.toString();
		}

	}

	public class field_info extends MF_info {
		field_info(DataInput is) throws IOException {
			super(is);
		}
		public String toString() {
			return "field_info : ["
				+ lineSep
				+ super.toString()
				+ lineSep
				+ "]";
		}
	}

	public class method_info extends MF_info {
		method_info(DataInput is) throws IOException {
			super(is);
		}
		public String toString() {
			return "mehtod_info : ["
				+ lineSep
				+ super.toString()
				+ lineSep
				+ "]";
		}
	}

	public class attribute_info {

		short attribute_name_index;
		byte info[];

		attribute_info(DataInput is) throws IOException {
			attribute_name_index = is.readShort();
			int attribute_length = is.readInt();

			info = new byte[attribute_length];
			if (debug)
				 ((DebugInputStream) is).getInputStream().read(info);
			else
				 ((DataInputStream) is).read(info);

		}

		public String toString() {
			return "attribute_name_index = "
				+ attribute_name_index
				+ ", info = "
				+ info;
		}
	}
    
    public class code_attribute extends attribute_info {
        
        short max_stack;
        short max_locals;
        int code_length;
        byte code[];
        // No exception table yet
        
        
        code_attribute(attribute_info attr) throws IOException {
            super(new DataInputStream(new ByteArrayInputStream(new byte[] {
                   0,0,0,0,0,0 
            })));
            attribute_name_index=attr.attribute_name_index;
            
            info=attr.info;
            init(info);
        }
        
        private void init(byte []info) throws IOException {
            DataInput is2 = new DataInputStream(new ByteArrayInputStream(info));
            max_stack=is2.readShort();
            max_locals=is2.readShort();
            code_length=is2.readInt();
            code=new byte[code_length];
            for(int i=0;i<code_length;i++) {
                code[i]=is2.readByte();
            }
        }
        
        
    }

	/**
	 * This class mirrors exactly the class file structure, providing basic
	 * translation and filtering services.
	 */
	public class classfile {

		String className; // The given class name under reading

		int magic;
		short minor_version;
		short major_version;
		cp_info constant_pool[];
		short access_flags;
		short this_class;
		short super_class;
		short interfaces[];
		field_info fields[];
		method_info methods[];
		attribute_info attributes[];

		public classfile(String className, byte[] b)
			throws IOException, ClassFormatError {
			this(
				className,
				new DataInputStream(new ByteArrayInputStream(b, 0, b.length)));
		}

		public classfile(byte[] b) throws IOException, ClassFormatError {
			this(
				null,
				new DataInputStream(new ByteArrayInputStream(b, 0, b.length)));
			this.className = getCPClassName(true);
		}

		public classfile(InputStream is) throws IOException, ClassFormatError {
		    this(is, true);
		}
		
		public classfile(InputStream is, boolean closeStream) throws IOException, ClassFormatError {
			this(null, new DataInputStream(is), closeStream);
			this.className = getCPClassName(true);
		}

		public classfile(String className, DataInputStream _is) throws IOException, ClassFormatError {
		    this(className, _is, true);
		}
		
		public classfile(String className, DataInputStream _is, boolean closeStream)
			throws IOException, ClassFormatError {

			this.className = className;

			DataInput is = _is;
			if (debug)
				is = new DebugInputStream(_is);

			magic = is.readInt();
			minor_version = is.readShort();
			major_version = is.readShort();
			short constant_pool_count = is.readShort();

			if (debug)
				System.out.println(
					"magic = "
						+ magic
						+ System.getProperties().getProperty("line.separator")
						+ "minor_version = "
						+ minor_version
						+ System.getProperties().getProperty("line.separator")
						+ "major_version = "
						+ major_version
						+ System.getProperties().getProperty("line.separator")
						+ "constant_pool_count = "
						+ constant_pool_count);

			constant_pool = new cp_info[constant_pool_count];
			for (int i = 1; i < constant_pool_count; i++) {
				constant_pool[i] = get_cp_info(is);
				// Remember, 8bytes values take up 2 entries in constant pool
				// (Damn JG, WHY this?)
				if ((constant_pool[i] instanceof C_Long)
					|| (constant_pool[i] instanceof C_Double))
					i++;
			}

			access_flags = is.readShort();
			this_class = is.readShort();
			super_class = is.readShort();

			if (debug)
				System.out.println(
					"access_flags = "
						+ access_flags
						+ System.getProperties().getProperty("line.separator")
						+ "this_class = "
						+ this_class
						+ System.getProperties().getProperty("line.separator")
						+ "super_class = "
						+ super_class);

			short interfaces_count = is.readShort();
			interfaces = new short[interfaces_count];
			for (int i = 0; i < interfaces_count; i++) {
				interfaces[i] = is.readShort();
			}

			short fields_count = is.readShort();
			fields = new field_info[fields_count];
			for (int i = 0; i < fields_count; i++)
				fields[i] = new field_info(is);

			short methods_count = is.readShort();
			methods = new method_info[methods_count];
			for (int i = 0; i < methods_count; i++)
				methods[i] = new method_info(is);

			short attributes_count = is.readShort();
			attributes = new attribute_info[attributes_count];
			for (int i = 0; i < attributes_count; i++) {
				attributes[i] = new attribute_info(is);
            }

			if (closeStream) {
				if (is instanceof InputStream)
					 ((InputStream) is).close();
				else
					 ((DebugInputStream) is).close();
			}
		}

		/**
		 * @return the name of the class specified at construction
		 */
		public String getClassName() {
			return className;
		}

		/**
		 * Return true if the classfile contains an interface class *
		 * 
		 * @return true if the classfile contains an interface class
		 */
		public boolean isInterface() {
			return (access_flags & ACC_INTERFACE) > 0;
		}

		/**
		 * *
		 * 
		 * @return the name of the class specified by this classfile
		 */
		public String getCPClassName(boolean externalize) {
			String name =
				((C_Utf8) constant_pool[((C_Class) constant_pool[this_class])
					.name_index])
					.getBytesAsString();
			if (externalize)
				name = name.replace('/', '.');
			return name;
		}

		public String getCPClassName() {
			return getCPClassName(false);
		}

		public String getSuperClass(boolean externalize) {
			String name =
				((C_Utf8) constant_pool[((C_Class) constant_pool[super_class])
					.name_index])
					.getBytesAsString();
			if (externalize)
				name = name.replace('/', '.');
			return name;
		}

		public String getSuperClass() {
			return getSuperClass(false);
		}

		public String[] getInterfaces() {
			String[] result = new String[interfaces.length];
			for (int i = 0; i < interfaces.length; i++) {
				String name =
					(
						(C_Utf8) constant_pool[(
							(C_Class) constant_pool[interfaces[i]])
						.name_index])
						.getBytesAsString();
				result[i] = name;
			}
			return result;
		}

		/**
		 * Retrieves all the constants in the pool which have a particular tag
		 */
		private cp_info[] getConstByTag(short tag) {
			Vector v = new Vector();
			for (int i = 1; i < constant_pool.length; i++) {
				if (constant_pool[i].tag == tag)
					v.addElement(constant_pool[i]);
				if ((constant_pool[i] instanceof C_Long)
					|| (constant_pool[i] instanceof C_Double))
					i++;
			}

			cp_info[] cpi = new cp_info[v.size()];
			v.copyInto(cpi);
			return cpi;
		}

		/**
		 * This method detects the presence of "Class.forName()" call in this
		 * classfile.
		 * 
		 * @return true if the class refers to "Class.forName()" methods
		 */
		public boolean forNameCalled() {
			// Get CONSTANT_Class entries
			cp_info[] cIndex = getConstByTag(CONSTANT_Class);

			int classClass_index = -1;
			for (int i = 0; i < cIndex.length; i++) {
				String clsName =
					((C_Utf8) constant_pool[((C_Class) cIndex[i]).name_index])
						.getBytesAsString();
				// Locate the position of "Class" if found
				if ("java/lang/Class".equals(clsName))
					for (int j = 0; j < constant_pool.length; j++)
						if (constant_pool[j] instanceof C_Class)
							if (((C_Class) constant_pool[j]).name_index
								== ((C_Class) cIndex[i]).name_index)
								classClass_index = j;
			}

			// Now, if we have "Class" - let's see
			// if some MethodRef refers to "forName"
			if (classClass_index != -1) {
				cp_info[] mIndex = getConstByTag(CONSTANT_Methodref);

				for (int i = 0; i < mIndex.length; i++)
					if (((C_Methodref) mIndex[i]).class_index
						== classClass_index)
						// Now we have a method of "Class".. let's see if it's
						// "Class forName(String)".
						if ("forName"
							.equals(
								(
									(C_Utf8) constant_pool[(
										(C_NameAndType) constant_pool[(
											(C_Methodref) mIndex[i])
									.name_and_type_index])
									.name_index])
									.getBytesAsString())
							&& "(Ljava/lang/String;)Ljava/lang/Class;".equals(
								(
									(C_Utf8) constant_pool[(
										(C_NameAndType) constant_pool[(
											(C_Methodref) mIndex[i])
									.name_and_type_index])
									.descriptor_index])
									.getBytesAsString()))
							// Got it.
							return true;
			}

			return false;
		}

		/**
		 * Retrieve the classes known to this .class file <b>Note:</b> The
		 * classes are returned in internal form.
         * <p>
         * The classes returned are the one listed in the method's signatures, not
         * the one used in the body.
		 */
		public String[] getUsedClasses() {
		    return getUsedClasses(false);
        }
            
        public String[] getUsedClasses(boolean includeMethodBody) {
			Set usedClassesSet = new HashSet();
			// Get CONSTANT_Class entries
			cp_info[] cIndex = getConstByTag(CONSTANT_Class);

			// Retrieve class names for each entry
			for (int i = 0; i < cIndex.length; i++) {
				// A ClassCast or ArrayOutOfBounds exception here means
				// something
				// has gone *really* wrong...
				usedClassesSet.add(
					((C_Utf8) constant_pool[((C_Class) cIndex[i]).name_index])
						.getBytesAsString());
			}

			// For each method, look into the signature to
			// discover the names of the classes in the return type
			// and parameters
			for (int i = 0; i < methods.length; i++) {
				String mSignature =
					((C_Utf8) constant_pool[methods[i].descriptor_index])
						.getBytesAsString();
				//String mName=
				// ((C_Utf8)constant_pool[methods[i].name_index]).getBytesAsString();
				SignatureAnalyzer sa =
					new SignatureAnalyzer(mSignature, true, true);
				/*
				 * System.out.print(sa.getReturnTypeName()+" "+mName+"(");
				 * while(sa.hasMoreParameters()) {
				 * System.out.print(sa.nextParameterTypeName()); if
				 * (sa.hasMoreParameters()) System.out.print(","); }
				 * System.out.println(")");
				 */
				while (sa.hasMoreParameters()) {
					usedClassesSet.add(sa.nextParameterTypeName());
				}
				if (sa.getReturnTypeName() != null)
					usedClassesSet.add(sa.getReturnTypeName());
                
                if (includeMethodBody) usedClassesSet.addAll(extractInvokedMethods(methods[i], true).keySet());
			}

			String[] names = new String[usedClassesSet.size()];
			usedClassesSet.toArray(names);
			return names;
		}
        
        /**
         * Returns the methods <i>used</i> invoked by this class, i.e. the method invoked in each method.
         * @return the methods <i>used</i> invoked by this class, i.e. the method invoked in each method.
         */
        public Map getUsedMethods() {
            Map result = new HashMap();

            // For each method, look into the signature to
            // discover the names of the classes in the return type
            // and parameters
            for (int i = 0; i < methods.length; i++) {
                Map usedMethods = extractInvokedMethods(methods[i], true);
                for (Iterator j = usedMethods.keySet().iterator(); j.hasNext();) {
                    String className = (String) j.next();
                    Set s = (Set) result.get(className);
                    if (s == null)
                        result.put(className, s = new HashSet());
                    Set usedMethodsSet = (Set) usedMethods.get(className);
                    s.addAll(usedMethodsSet);
                }
            }
            return result;
        }

		private Map extractInvokedMethods(method_info mi, boolean externalize) {
            HashMap results = new HashMap();
            
            C_Utf8 methodName = (C_Utf8)constant_pool[mi.name_index];
            
            for(int i=0;i<mi.attributes.length;i++) {
                int attributeNameIndex=mi.attributes[i].attribute_name_index;
                
                C_Utf8 name = (C_Utf8)constant_pool[attributeNameIndex];
                if (name.getBytesAsString().equals("Code")) {
                    try {
                        code_attribute attr = new code_attribute(mi.attributes[i]);
                        byte [] byteCode = attr.code;
                        ByteCodeAnalyzer bca = new ByteCodeAnalyzer(this, methodName.getBytesAsString(), byteCode);
                        Map invokedMethods = bca.findInvokedClassesAndMethods(ByteCodeAnalyzer.INVOKE_VIRTUAL | ByteCodeAnalyzer.INVOKE_STATIC, externalize);
                        for(Iterator j = invokedMethods.keySet().iterator();j.hasNext();) {
                            String invokedClsName=(String)j.next();
                            Set s = (Set)results.get(invokedClsName);
                            if (s==null) results.put(invokedClsName, s=new HashSet());
                            Set s2 = (Set)invokedMethods.get(invokedClsName);
                            s.addAll(s2);
                        }
                    } catch(IOException e) {
                        throw new RuntimeException("An unexpected IOException has occurred", e);
                    }

                }
            }
            
            return results;
            
        }
        
        
        public String toString() {
			StringBuffer sb = new StringBuffer();
			synchronized (sb) {
				sb.append(
					"magic = "
						+ magic
						+ lineSep
						+ "minor_version = "
						+ minor_version
						+ lineSep
						+ "major_version = "
						+ major_version
						+ lineSep);

				for (int i = 1; i < constant_pool.length; i++) {
					sb.append(
						"constant_pool["
							+ i
							+ "] = "
							+ constant_pool[i].toString());
					sb.append(lineSep);
					if ((constant_pool[i] instanceof C_Long)
						|| (constant_pool[i] instanceof C_Double))
						i++;
				}

				sb.append(
					"access_flags = "
						+ access_flags
						+ lineSep
						+ "this_class = "
						+ this_class
						+ lineSep
						+ "super_class = "
						+ super_class
						+ lineSep);

				sb.append(interfaces.length + " interface(s)");
				sb.append(lineSep);
				for (int i = 0; i < interfaces.length; i++) {
					sb.append("interfaces[" + i + "] = " + interfaces[i]);
					sb.append(lineSep);
				}

				sb.append(fields.length + " field(s)");
				sb.append(lineSep);
				for (int i = 0; i < fields.length; i++) {
					sb.append("fields[" + i + "] = " + fields[i]);
					sb.append(lineSep);
				}

				sb.append(methods.length + " method(s)");
				sb.append(lineSep);
				for (int i = 0; i < methods.length; i++) {
					sb.append("methods[" + i + "] = " + methods[i]);
					sb.append(lineSep);
				}

				sb.append(attributes.length + " attribute(s)");
				sb.append(lineSep);
				for (int i = 0; i < attributes.length; i++) {
					sb.append("attributes[" + i + "] = " + attributes[i]);
					sb.append(lineSep);
				}
				return sb.toString();
			}
		}

		int n = 1;

		// Reads a cp_info from the given stream
		private cp_info get_cp_info(DataInput is)
			throws IOException, ClassFormatError {

			//byte tag = is.readByte();
			int tag = is.readUnsignedByte();

			if (debug)
				System.out.print((n++) + " Tag: " + (int) tag + " -> ");

			if (tag == 0)
				System.out.println(" next = " + is.readUnsignedByte());

			cp_info cpi = null;
			;

			switch ((int) tag) {
				case CONSTANT_Class :
					cpi = new C_Class(is);
					break;
				case CONSTANT_Fieldref :
					cpi = new C_Fieldref(is);
					break;
				case CONSTANT_Methodref :
					cpi = new C_Methodref(is);
					break;
				case CONSTANT_InterfaceMethodref :
					cpi = new C_InterfaceMethodref(is);
					break;
				case CONSTANT_String :
					cpi = new C_String(is);
					break;
				case CONSTANT_Integer :
					cpi = new C_Integer(is);
					break;
				case CONSTANT_Float :
					cpi = new C_Float(is);
					break;
				case CONSTANT_Long :
					cpi = new C_Long(is);
					break;
				case CONSTANT_Double :
					cpi = new C_Double(is);
					break;
				case CONSTANT_NameAndType :
					cpi = new C_NameAndType(is);
					break;
				case CONSTANT_Utf8 :
					cpi = new C_Utf8(is);
					break;
				default :
					System.out.println(
						"\nConstant Pool tag unknown found in class "
							+ className
							+ "!! (apparent value: "
							+ tag
							+ (debug
								? ", read count : "
									+ ((DebugInputStream) is).getReadCount()
								: "")
							+ ")");
					//cpi= new unknown_cp_info();
					throw new ClassFormatError();
			}
			if (debug)
				System.out.println(cpi);
			return cpi;
		}

        public void dumpConstantPool(PrintStream out) {
            out.println("Constant pool dump");
            out.println("==================");
            out.println();
            for(int i=0;i<constant_pool.length;i++) {
                out.print(i+" )");
                out.println(constant_pool[i]);
            }
        }
	}

	/**
	 * Buids a CPoolReader which uses a JDK12ClassFileFinder
	 */
	public CPoolReader() {
		this(new JDK12ClassFileFinder());
	}

	/**
	 * Buids a CPoolReader which uses a JDK12 with the given application class
	 * path
	 * 
	 * @param classPath
	 *            the application class path to use
	 */
	public CPoolReader(String classPath) {
		this(new JDK12ClassFileFinder(classPath));
	}

	/**
	 * Buids a CPoolReader which uses the specifies ClassFileFinder
	 * 
	 * @param cff
	 *            the ClassFileFinder to use for locating class bytecode
	 */
	public CPoolReader(ClassFileFinder cff) {
		this.cff = cff;
	}

	/**
	 * This returns an interpreted class file. The object returned can be a
	 * cached instance.
	 * 
	 * @return a classfile instance with the information about the class
	 * @exception IOException
	 *                if an I/O Exception occurs reading the class file
	 * @exception ClassNotFoundException
	 *                if the class is not found
	 */
	public classfile readClass(String className)
		throws IOException, ClassNotFoundException {
		if (classfileCache != null)
			if (classfileCache.getClassName().equals(className))
				return classfileCache;
		classfileCache =
			new classfile(
				className,
				new DataInputStream(cff.openClass(className)));
		return classfileCache;
	}

	public classfile readClassData(String className, InputStream is)
		throws IOException {
		return new classfile(className, new DataInputStream(is));
	}

	public classfile readClassData(InputStream is) throws IOException {
		return new classfile(new DataInputStream(is));
	}
	
	public classfile readClassData(InputStream is, boolean closeStream) throws IOException {
		return new classfile(new DataInputStream(is), closeStream);
	}

	public classfile readClassData(byte[] data) throws IOException {
		return new classfile(
			new DataInputStream(new ByteArrayInputStream(data)));
	}

	/**
	 * Retrieve the classes known to this .class file <b>Note:</b> The
	 * classes are returned in <b>external</b> form
	 * <p>
	 * Refer to class description for finding/loading scheme discussion
	 * 
	 * @param the
	 *            name of the class to inspect
	 * @return an array containing the external-form fully qualified names of
	 *         the classes used by the class object
	 */
	public String[] getUsedClasses(String name) throws IOException, ClassNotFoundException {
	    return getUsedClasses(name, false);
    }
    
    /**
     * Retrieve the classes known to this .class file <b>Note:</b> The
     * classes are returned in <b>external</b> form
     * <p>
     * Refer to class description for finding/loading scheme discussion
     * 
     * @param the
     *            name of the class to inspect
     * @return an array containing the external-form fully qualified names of
     *         the classes used by the class object
     */
    public String[] getUsedClasses(String name, boolean includeMethodBody) 
		throws IOException, ClassNotFoundException {
		classfile cf = readClass(name);
        
		String[] names = cf.getUsedClasses(includeMethodBody);

		// Externalize form
		for (int i = 0; i < names.length; i++)
			names[i] = names[i].replace('/', '.');

		return names;
	}

	/**
	 * This method detects the presence of "Class.forName()" call in this
	 * classfile.
	 * 
	 * @param the
	 *            name of the class to inspect
	 * @return true if the class refers to "Class.forName()" methods
	 */
	public boolean forNameCalled(String name)
		throws IOException, ClassNotFoundException {
		return readClass(name).forNameCalled();
	}

	public static void main(String args[]) throws Exception {

		if (args.length == 0) {
			System.out.println("CPoolReader [-debug] <class name>");
			System.exit(0);
		}

		int cIndex = 0;
		if (args.length > 1) {
			if ("-debug".equals(args[0]))
				debug = true;
			cIndex++;
		}

		CPoolReader cpr = new CPoolReader();
		classfile cf = cpr.readClass(args[cIndex]);

		if (!debug) {
			System.out.println(cf);

			System.out.println();
			System.out.println("Used classes: ");
			String[] names = cf.getUsedClasses();
			for (int i = 0; i < names.length; i++)
				System.out.println(names[i]);
		}
	}
}