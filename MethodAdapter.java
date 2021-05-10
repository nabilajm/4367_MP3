

import java.io.IOException;

import org.objectweb.asm.*;

public class MethodAdapter extends MethodVisitor implements Opcodes {
	//int methodAccess;
	public MethodAdapter(MethodVisitor mv) {
        super(ASM5,mv);
        //boolean isStatic = (methodAccess & Opcodes.ACC_STATIC) != 0;
        //boolean isSynchronized = (methodAccess & Opcodes.ACC_SYNCHRONIZED) != 0;
    }
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
    	switch (opcode) {
        	case INVOKEVIRTUAL:
        		//check if it is "Thread.start()"
        		if(isThreadClass(owner)&&name.equals("start")&&desc.equals("()V")) {
	            	mv.visitInsn(DUP);
	        		mv.visitMethodInsn(INVOKESTATIC, "Log", "logStart", "(Ljava/lang/Thread;)V",false);
				}//check if it is "Thread.join()"
        		else if(isThreadClass(owner)&&name.equals("join")&&desc.equals("()V")) {
	            	mv.visitInsn(DUP);
	        		mv.visitMethodInsn(INVOKESTATIC, "Log", "logJoin", "(Ljava/lang/Thread;)V",false);
    			} //check if it is "Object.wait()"
            	else if(name.equals("wait") && (desc.equals("()V")||desc.equals("(J)V")||desc.equals("(JI)V"))) {
	            	mv.visitInsn(DUP);
	        		mv.visitMethodInsn(INVOKESTATIC, "Log", "logWait",
	        				"(Ljava/lang/Object;)V",false);
        		} //check if it is "Object.notify()"
                else if(name.equals("notify")&&desc.equals("()V")) {
	            	mv.visitInsn(DUP);
	     		mv.visitMethodInsn(INVOKESTATIC, "Log", "logNotify",
	        				"(Ljava/lang/Object;)V",false);
            	}//check if it is "Object.notifyAll()"
                else if(name.equals("notifyAll")&&desc.equals("()V")) {
	            	mv.visitInsn(DUP);
	        		mv.visitMethodInsn(INVOKESTATIC, "Log", "logNotifyAll",
	        				"(Ljava/lang/Object;)V",false);
                				}
        	default: mv.visitMethodInsn(opcode, owner, name, desc,itf);
    	}

    }

    
    @Override
    public void visitInsn(int opcode) {
        boolean isStatic = (opcode & Opcodes.ACC_STATIC) != 0;
        boolean isSynchronized = (opcode & Opcodes.ACC_SYNCHRONIZED) != 0;
    	switch (opcode) {
    		case Opcodes.MONITORENTER:
    			mv.visitInsn(Opcodes.DUP);
    			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Log", "logLock","(Ljava/lang/Object;)V",false);
    			break;
    		case Opcodes.MONITOREXIT:
    				mv.visitInsn(Opcodes.DUP);
    				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Log", "logUnlock","(Ljava/lang/Object;)V",false);
    				break;
    		case Opcodes.IRETURN:
    		case Opcodes.LRETURN:
    		case Opcodes.FRETURN:
    		case Opcodes.DRETURN:
    		case Opcodes.ARETURN:
    		case Opcodes.RETURN:
    		case Opcodes.ATHROW:
    		{
    			if(isSynchronized){
    				if(isStatic){
    					mv.visitInsn(Opcodes.ACONST_NULL);
    					mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Log", "logUnlock","(Ljava/lang/Object;)V",false);
    				}
    				else{
    					mv.visitVarInsn(Opcodes.ALOAD, 0);
    					mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Log", "logUnlock","(Ljava/lang/Object;)V",false);
    				}
    			}
    		}
    		case AALOAD:case BALOAD:case CALOAD:case SALOAD:case IALOAD:case FALOAD:case
    		DALOAD:case LALOAD:
    			mv.visitInsn(opcode);
    			break;
    		case AASTORE:case BASTORE:case CASTORE:case SASTORE:case IASTORE:case FASTORE:
    			mv.visitInsn(opcode);
    			break;
    		case DASTORE:case LASTORE:
    			mv.visitInsn(opcode);
    			break;
    		default:break;
    	}
    	mv.visitInsn(opcode);
    }

    private boolean isThreadClass(String cname)
    {
    	while(!cname.equals("java/lang/Object"))
    	{
    		if(cname.equals("java/lang/Thread"))
    			return true;

    		try {
				ClassReader cr= new ClassReader(cname);
				cname = cr.getSuperName();
			} catch (IOException e) {
				return false;
			}
    	}
    	return false;
    }
    
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
    	//stackFrame = new ArrayList<>();
    	char firstDescriptorChar = desc.charAt(0);
        boolean longOrDouble = firstDescriptorChar == 'J' || firstDescriptorChar == 'D';
    switch (opcode) {
    	case GETSTATIC:
    		mv.visitFieldInsn(Opcodes.GETSTATIC, owner, name, desc);
    		if (longOrDouble) {
        		mv.visitFieldInsn(Opcodes.GETSTATIC, owner, name, desc);
    		}
    		break;
    	case PUTSTATIC:
    		 mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, name, desc);
     		if (longOrDouble) {
        		mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, name, desc);
    		}
	    	break;
	    case GETFIELD:
    		if (longOrDouble) {
        		mv.visitFieldInsn(Opcodes.GETFIELD, owner, name, desc);
    		}
	    	break;
    	case PUTFIELD:
    		mv.visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
    		mv.visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
    		if (longOrDouble) {
        		mv.visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
    		}
    		//this part is slightly more complicated
    	default: break;
    	}
    	mv.visitFieldInsn(opcode, owner, name, desc);
    	}
    
    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
    	mv.visitMaxs(10, 10); // set X and Y to a proper value
    }

}