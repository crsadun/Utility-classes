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

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

public class DebugInputStream implements DataInput {
   
     private DataInputStream dis;
     protected long debug_readcount=0;
   
     public DebugInputStream(DataInputStream dis) {
         this.dis=dis;
     }
   
     public void readFully(byte b[]) throws IOException{ dis.readFully(b); debug_readcount+=b.length; }
     public void readFully(byte b[], int off, int len) throws IOException{ dis.readFully(b, off, len); debug_readcount+=len; }
     public int skipBytes(int n) throws IOException{ debug_readcount+=n; return dis.skipBytes(n); }
     public boolean readBoolean() throws IOException{ debug_readcount+=1 ; return dis.readBoolean(); }
     public byte readByte() throws IOException{ debug_readcount+=1; return dis.readByte(); }
     public int readUnsignedByte() throws IOException{ debug_readcount+=1 ; return dis.readUnsignedByte(); }
     public short readShort() throws IOException{ debug_readcount+=2 ; return dis.readShort(); }
     public int readUnsignedShort() throws IOException{ debug_readcount+=2; return dis.readUnsignedShort(); }
     public char readChar() throws IOException{ debug_readcount+=2 ; return dis.readChar() ; }
     public int readInt() throws IOException{ debug_readcount+=4 ; return dis.readInt(); }
     public long readLong() throws IOException{ debug_readcount+=8 ; return dis.readLong(); }
     public float readFloat() throws IOException{ debug_readcount+=4 ; return dis.readFloat(); }
     public double readDouble() throws IOException{ debug_readcount+=8 ; return dis.readDouble(); }
     public String readLine() throws IOException{ throw new RuntimeException("readLine not supported in DebugInputStream"); }
     public String readUTF() throws IOException{ 
      String s = dis.readUTF(); 
      int utflen = s.length();
      
      int count = 0;
	   
   	while (count < utflen) {
   	   int c = (int)s.charAt(count);
   	   switch (c >> 4) {
	       case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
	         count++;
	         break;
	       case 12: case 13:
		      // 110x xxxx   10xx xxxx
		      count += 2;  
		      break;
		    case 14:
		      // 1110 xxxx  10xx xxxx  10xx xxxx
		      count += 3;  
		      break;
   	   }
   	}
   	
   	debug_readcount+=count+2;
   	return s;
      
     }
     
     public void close() throws IOException {
      dis.close();
     }

     public DataInputStream getInputStream() { return dis; }
     public long getReadCount() { return debug_readcount; }
}