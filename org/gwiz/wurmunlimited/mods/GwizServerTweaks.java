/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * 
 * For more information, please refer to <http://unlicense.org/>
*/

package org.gwiz.wurmunlimited.mods;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.Versioned;
import org.gotti.wurmunlimited.modloader.interfaces.WurmServerMod;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class GwizServerTweaks implements WurmServerMod, Configurable, PreInitable, Versioned {

	private static final String version = "0.65";
	private static Logger logger = Logger.getLogger(GwizServerTweaks.class.getName());
	private static boolean allowInterfaithLink = true;

	@Override
	public void configure(Properties properties) {
		allowInterfaithLink = Boolean.parseBoolean(properties.getProperty("allowInterfaithLink", "true"));
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public void preInit() {
		ClassPool hookClassPool = HookManager.getInstance().getClassPool();

		// Allow inter-faith priest linking
		if (allowInterfaithLink) {
			try {
				CtClass ctCreatureBehaviour = hookClassPool
						.getCtClass("com.wurmonline.server.behaviours.CreatureBehaviour");
				ctCreatureBehaviour.getDeclaredMethod("handle_MAGICLINK").instrument(new ExprEditor() {
					public void edit(MethodCall methodCall) throws CannotCompileException {
						if (methodCall.getMethodName().equals("getTemplateDeity")) {
							methodCall.replace("{ $_ = 0; }");
						}
					}
				});
				logger.log(Level.INFO, "Inter-faith linking now allowed.");
			} catch (NotFoundException | CannotCompileException e) {
				logger.log(Level.WARNING, "Something went horribly wrong when allowing inter-faith linking!", e);
			}
		}
	}

}