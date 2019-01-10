package com.deltax.util;

public class MethodData implements Comparable {

    private String packageName;
    private String unqualifiedClassName;
    private String methodName;
    private String returnTypeName;
    private String parametersList;
    
    
    public MethodData(String clsName, String methodName, String returnTypeName,
            String parametersList) {
        this.methodName=methodName;
        this.returnTypeName=returnTypeName;
        this.packageName=extractPkgName(clsName);
        this.unqualifiedClassName=extractClsName(clsName);
        this.parametersList=parametersList;
    }
    
    private String extractClsName(String clsName) {
        int i=clsName.lastIndexOf('.');
        if (i==-1) return clsName;
        else return clsName.substring(i+1);
    }

    private String extractPkgName(String clsName) {
        int i=clsName.lastIndexOf('.');
        if (i==-1) return "default";
        else return clsName.substring(0,i);
    }

    public String getUnqualifiedClassName() {
        return unqualifiedClassName;
    }
    
    public String getQualifiedClsName() {
        return packageName+"."+unqualifiedClassName;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getParametersList() {
        return parametersList;
    }

    public String getReturnTypeName() {
        return returnTypeName;
    }

    public String toString() {
        return returnTypeName+" "+getQualifiedClsName()+"."+methodName+"("+parametersList+")";
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof MethodData) {
            MethodData md = (MethodData)obj;
            return md.getPackageName().equals(packageName) &&
                    md.getUnqualifiedClassName().equals(unqualifiedClassName) &&
                    md.getMethodName().equals(methodName) &&
                    md.getParametersList().equals(parametersList) &&
                    md.getReturnTypeName().equals(returnTypeName);
        } else return false;
    }
    
    public int hashCode() { return toString().hashCode(); }
    
    public int compareTo(Object o) {
        if (o instanceof MethodData)  {
            MethodData uc = (MethodData)o;
            
            String sig1=getQualifiedClsName()+"."+getMethodName()+": "+getReturnTypeName();
            String sig2=uc.getQualifiedClsName()+"."+uc.getMethodName()+": "+uc.getReturnTypeName();
            
            return sig1.compareTo(sig2);
        } else throw new RuntimeException("attempting to compare a "+o.getClass().getName()+" to a "+getClass().getName());
    }
}
