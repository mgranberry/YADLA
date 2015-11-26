package com.kludgenics.alrightypump.device.dexcom.g5

/**
 * Created by matthias on 11/24/15.
 */
import com.kludgenics.alrightypump.device.dexcom.g5.StateMachine.State.*

class StateMachine {
    sealed class State {
        public class AwaitingBond : State()
        object Bonded : State()
        object Connected : State()
        object AwaitingConnection : State()
    }
    var state: State = AwaitingBond()


}