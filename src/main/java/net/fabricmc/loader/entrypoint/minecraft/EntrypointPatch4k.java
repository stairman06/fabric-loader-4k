package net.fabricmc.loader.entrypoint.minecraft;

import net.fabricmc.loader.entrypoint.EntrypointPatch;
import net.fabricmc.loader.entrypoint.EntrypointTransformer;
import net.fabricmc.loader.launch.common.FabricLauncher;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import java.util.ListIterator;
import java.util.function.Consumer;

public class EntrypointPatch4k extends EntrypointPatch {
	public EntrypointPatch4k(EntrypointTransformer transformer) {
		super(transformer);
	}

	@Override
	public void process(FabricLauncher launcher, Consumer<ClassNode> classEmitter) {
		try {
			String entrypoint = launcher.getEntrypoint();
			ClassNode mainClass = loadClass(launcher, entrypoint);

			MethodNode mainMethod = findMethod(mainClass, (method) -> method.name.equals("<init>"));
			if (mainMethod == null) {
				throw new RuntimeException("Could not find main method in " + entrypoint + "!");
			}

			ListIterator<AbstractInsnNode> it = mainMethod.instructions.iterator();

			moveBefore(it, Opcodes.RETURN);
			it.add(new InsnNode(Opcodes.ACONST_NULL));
			it.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/fabricmc/loader/entrypoint/applet/AppletMain", "hookGameDir", "(Ljava/io/File;)Ljava/io/File;", false));
			it.add(new VarInsnNode(Opcodes.ALOAD, 0));
			it.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "net/fabricmc/loader/entrypoint/minecraft/hooks/EntrypointClient", "start", "(Ljava/io/File;Ljava/lang/Object;)V", false));

			classEmitter.accept(mainClass);

			EntrypointTransformer.appletMainClass = entrypoint;
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
}
