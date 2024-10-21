package org.nasdanika.launcher.demo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.nasdanika.cli.SubCommandCapabilityFactory;
import org.nasdanika.common.ProgressMonitor;

import picocli.CommandLine;

public class ModuleGraphCommandFactory extends SubCommandCapabilityFactory<ModuleGraphCommand> {

	@Override
	protected Class<ModuleGraphCommand> getCommandType() {
		return ModuleGraphCommand.class;
	}
	
	@Override
	protected CompletionStage<ModuleGraphCommand> doCreateCommand(
			List<CommandLine> parentPath, 
			Loader loader,
			ProgressMonitor progressMonitor) {
		return CompletableFuture.completedStage(new ModuleGraphCommand());
	}

}
