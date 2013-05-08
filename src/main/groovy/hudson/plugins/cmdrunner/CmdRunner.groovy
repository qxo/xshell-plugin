package hudson.plugins.cmdrunner

import org.apache.commons.lang.SystemUtils

class CmdRunner {

	static main(args) {
		println "args:${args}";
	//	executeCmd(args)
	}


//	public CmdRunner(){
//		this(SystemUtils.IS_OS_WINDOWS);
//	}


	public CmdRunner(Boolean isWin=null){
		if(isWin == null ? SystemUtils.IS_OS_WINDOWS: isWin){
			CMD="cmd";
			C1 = "/c";
		}else{
			CMD="sh"
			C1="-c";
		}
	}
	
	private  final String CMD;

	private  final String C1;

	private static final def NEED_CMD_PATTERN =  /^((mvn)|(ant)|(java)|(\S+[.]bat\s+))|([^<|>"][<|>"]{1,2}[^<|>"])/


	public  Process executeCmd(def cmd,File workingDir = null,Closure callback =null,boolean forceAppendCmd=false){
		cmd = parseCmd(cmd,forceAppendCmd)

		println "runnng cmd: \t ${cmd}"
		def builder = new ProcessBuilder(cmd);
		if( null != workingDir){
			builder.directory(workingDir)
		}

		if(callback== null){
			callback = {Process  proc,ProcessBuilder  b ->
				if( null ==proc && b != null){
					proc = b.start();
				}
				proc.inputStream.eachLine{ println it; }
				proc.waitFor() ;
				def exitValue = proc.exitValue();
				if( exitValue){
					println "cmd error!exitValue:${exitValue}";
					proc.getErrorStream().eachLine { println it; }
				}else{
					println "cmd << ${cmd} >> execute success:)"
				}
				return proc;
			}
		}
		if( callback.getParameterTypes().size() < 2  ){
			def p = builder.start();
			return callback(p);
		}else{
			return callback(null,builder);
		}
	}

	public  parseCmd(def cmd,boolean forceAppendCmd = false) {
		assert cmd != null;
		switch(cmd){
			case CharSequence :
				cmd = cmd.trim(); //解决命令中多余的空格
				def cmdMatcher =cmd  =~ /^\s*${CMD}\s+${C1}/;
				if(cmdMatcher){
					String cmdPrefix = cmdMatcher.group();
					cmd = (  [CMD, C1]<< cmd.substring(cmdPrefix.length()) );
				}else if( forceAppendCmd || cmd  =~ NEED_CMD_PATTERN  ){
					cmd =[CMD, C1, cmd]
				}
				break;
			default:
				cmd = cmd.collect{
					//解决命令中多余的空格
					return it instanceof String ? it.trim() : it;
				}
				if( cmd[0] != CMD  &&  cmd[1] != C1 &&( forceAppendCmd ||  cmd.find{ it =~ NEED_CMD_PATTERN  } ) ){
					cmd = [CMD, C1]+ cmd
				}
		}
		return cmd
	}
}