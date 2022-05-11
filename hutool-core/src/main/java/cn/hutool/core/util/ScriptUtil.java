package cn.hutool.core.util;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.map.WeakConcurrentMap;
import cn.hutool.core.text.StrUtil;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * 脚本工具类
 *
 * @author Looly
 */
public class ScriptUtil {

	private static final ScriptEngineManager MANAGER = new ScriptEngineManager();
	private static final WeakConcurrentMap<String, ScriptEngine> CACHE = new WeakConcurrentMap<>();

	/**
	 * 获得单例的{@link ScriptEngine} 实例
	 *
	 * @param nameOrExtOrMime 脚本名称
	 * @return {@link ScriptEngine} 实例
	 */
	public static ScriptEngine getScript(final String nameOrExtOrMime) {
		return CACHE.computeIfAbsent(nameOrExtOrMime, () -> createScript(nameOrExtOrMime));
	}

	/**
	 * 创建 {@link ScriptEngine} 实例
	 *
	 * @param nameOrExtOrMime 脚本名称
	 * @return {@link ScriptEngine} 实例
	 * @since 5.2.6
	 */
	public static ScriptEngine createScript(final String nameOrExtOrMime) {
		ScriptEngine engine = MANAGER.getEngineByName(nameOrExtOrMime);
		if (null == engine) {
			engine = MANAGER.getEngineByExtension(nameOrExtOrMime);
		}
		if (null == engine) {
			engine = MANAGER.getEngineByMimeType(nameOrExtOrMime);
		}
		if (null == engine) {
			throw new NullPointerException(StrUtil.format("Script for [{}] not support !", nameOrExtOrMime));
		}
		return engine;
	}

	/**
	 * 获得单例的JavaScript引擎
	 *
	 * @return Javascript引擎
	 * @since 5.2.5
	 */
	public static ScriptEngine getJsEngine() {
		return getScript("js");
	}

	/**
	 * 创建新的JavaScript引擎
	 *
	 * @return Javascript引擎
	 * @since 5.2.6
	 */
	public static ScriptEngine createJsEngine() {
		return createScript("js");
	}

	/**
	 * 获得单例的Python引擎<br>
	 * 需要引入org.python:jython
	 *
	 * @return Python引擎
	 * @since 5.2.5
	 */
	public static ScriptEngine getPythonEngine() {
		System.setProperty("python.import.site", "false");
		return getScript("python");
	}

	/**
	 * 创建Python引擎<br>
	 * 需要引入org.python:jython
	 *
	 * @return Python引擎
	 * @since 5.2.6
	 */
	public static ScriptEngine createPythonEngine() {
		System.setProperty("python.import.site", "false");
		return createScript("python");
	}

	/**
	 * 获得单例的Lua引擎<br>
	 * 需要引入org.luaj:luaj-jse
	 *
	 * @return Lua引擎
	 * @since 5.2.5
	 */
	public static ScriptEngine getLuaEngine() {
		return getScript("lua");
	}

	/**
	 * 创建Lua引擎<br>
	 * 需要引入org.luaj:luaj-jse
	 *
	 * @return Lua引擎
	 * @since 5.2.6
	 */
	public static ScriptEngine createLuaEngine() {
		return createScript("lua");
	}

	/**
	 * 获得单例的Groovy引擎<br>
	 * 需要引入org.codehaus.groovy:groovy-all
	 *
	 * @return Groovy引擎
	 * @since 5.2.5
	 */
	public static ScriptEngine getGroovyEngine() {
		return getScript("groovy");
	}

	/**
	 * 创建Groovy引擎<br>
	 * 需要引入org.codehaus.groovy:groovy-all
	 *
	 * @return Groovy引擎
	 * @since 5.2.6
	 */
	public static ScriptEngine createGroovyEngine() {
		return createScript("groovy");
	}

	/**
	 * 执行Javascript脚本，返回Invocable，此方法分为两种情况：
	 *
	 * <ol>
	 *     <li>执行的脚本返回值是可执行的脚本方法</li>
	 *     <li>脚本为函数库，则ScriptEngine本身为可执行方法</li>
	 * </ol>
	 *
	 * @param script 脚本内容
	 * @return 执行结果
	 * @throws UtilException 脚本异常
	 * @since 5.3.6
	 */
	public static Invocable evalInvocable(final String script) throws UtilException {
		final ScriptEngine jsEngine = getJsEngine();
		final Object eval;
		try {
			eval = jsEngine.eval(script);
		} catch (final ScriptException e) {
			throw new UtilException(e);
		}
		if(eval instanceof Invocable){
			return (Invocable)eval;
		} else if(jsEngine instanceof Invocable){
			return (Invocable)jsEngine;
		}
		throw new UtilException("Script is not invocable !");
	}

	/**
	 * 执行有返回值的Javascript脚本
	 *
	 * @param script 脚本内容
	 * @return 执行结果
	 * @throws UtilException 脚本异常
	 * @since 3.2.0
	 */
	public static Object eval(final String script) throws UtilException {
		try {
			return getJsEngine().eval(script);
		} catch (final ScriptException e) {
			throw new UtilException(e);
		}
	}

	/**
	 * 执行有返回值的脚本
	 *
	 * @param script  脚本内容
	 * @param context 脚本上下文
	 * @return 执行结果
	 * @throws UtilException 脚本异常
	 * @since 3.2.0
	 */
	public static Object eval(final String script, final ScriptContext context) throws UtilException {
		try {
			return getJsEngine().eval(script, context);
		} catch (final ScriptException e) {
			throw new UtilException(e);
		}
	}

	/**
	 * 执行有返回值的脚本
	 *
	 * @param script   脚本内容
	 * @param bindings 绑定的参数
	 * @return 执行结果
	 * @throws UtilException 脚本异常
	 * @since 3.2.0
	 */
	public static Object eval(final String script, final Bindings bindings) throws UtilException {
		try {
			return getJsEngine().eval(script, bindings);
		} catch (final ScriptException e) {
			throw new UtilException(e);
		}
	}

	/**
	 * 执行JS脚本中的指定方法
	 *
	 * @param script js脚本
	 * @param func 方法名
	 * @param args 方法参数
	 * @return 结果
	 * @since 5.3.6
	 */
	public static Object invoke(final String script, final String func, final Object... args) {
		final Invocable eval = evalInvocable(script);
		try {
			return eval.invokeFunction(func, args);
		} catch (final ScriptException | NoSuchMethodException e) {
			throw new UtilException(e);
		}
	}

	/**
	 * 编译Javascript脚本
	 *
	 * @param script 脚本内容
	 * @return {@link CompiledScript}
	 * @throws UtilException 脚本异常
	 * @since 3.2.0
	 */
	public static CompiledScript compile(final String script) throws UtilException {
		try {
			return compile(getJsEngine(), script);
		} catch (final ScriptException e) {
			throw new UtilException(e);
		}
	}

	/**
	 * 编译Javascript脚本
	 *
	 * @param engine 引擎
	 * @param script 脚本内容
	 * @return {@link CompiledScript}
	 * @throws ScriptException 脚本异常
	 */
	public static CompiledScript compile(final ScriptEngine engine, final String script) throws ScriptException {
		if (engine instanceof Compilable) {
			final Compilable compEngine = (Compilable) engine;
			return compEngine.compile(script);
		}
		return null;
	}
}