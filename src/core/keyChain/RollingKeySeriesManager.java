package core.keyChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import core.config.Config;
import core.userDefinedTask.UserDefinedAction;

public abstract class RollingKeySeriesManager extends KeyStrokeManager {

	protected final RollingKeySeries currentRollingKeySeries;
	protected final List<UserDefinedAction> registeredActions;

	public RollingKeySeriesManager(Config config) {
		super(config);

		this.currentRollingKeySeries = new RollingKeySeries();
		this.registeredActions = new ArrayList<>();
	}

	@Override
	public final void startListening() {
		// Do nothing.
	}

	@Override
	public abstract Set<UserDefinedAction> onKeyStrokePressed(KeyStroke stroke);

	@Override
	public abstract Set<UserDefinedAction> onKeyStrokeReleased(KeyStroke stroke);

	@Override
	public final void clear() {
		currentRollingKeySeries.clearKeys();
	}

	@Override
	public final Set<UserDefinedAction> collision(Collection<TaskActivation> activations) {
		Set<UserDefinedAction> output = new HashSet<>();
		for (TaskActivation activation : activations) {
			for (UserDefinedAction action : registeredActions) {
				if (collisionWithAction(action, activation)) {
					output.add(action);
				}
			}
		}
		return output;
	}

	@Override
	public final Set<UserDefinedAction> registerAction(UserDefinedAction action) {
		Set<UserDefinedAction> toRemove = collision(action.getActivation());
		toRemove.forEach(a -> unRegisterAction(a));

		registeredActions.add(action);
		return toRemove;
	}

	@Override
	public final Set<UserDefinedAction> unRegisterAction(UserDefinedAction action) {
		Set<UserDefinedAction> output = new HashSet<>();
		for (Iterator<UserDefinedAction> iterator = registeredActions.iterator(); iterator.hasNext();) {
			UserDefinedAction existing = iterator.next();
			if (existing.equals(action)) {
				output.add(existing);
				iterator.remove();
			}
		}
		return output;
	}

	protected abstract boolean collisionWithAction(UserDefinedAction action, TaskActivation activation);

	/**
	 * Given a new key stroke coming in, consider start executing actions based on their activations.
	 * @return set of actions to execute.
	 */
	protected final Set<UserDefinedAction> considerTaskExecution(KeyStroke key) {
		if (key.getKey() == Config.HALT_TASK && getConfig().isEnabledHaltingKeyPressed()) {
			clear();
			return null;
		}
		return tasksToExecute();
	}

	protected abstract Set<UserDefinedAction> tasksToExecute();
}
