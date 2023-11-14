import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventIterator;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.event.WatchpointEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import java.util.Hashtable;
import java.util.List;

public class MyThread extends Thread {
    boolean connected = true;
    Hashtable ht = new Hashtable(40);
    GUI_Coverage_Tool mainFrame;
    int numClasses;
    String pkgName;
    ReferenceType rt;
    boolean stopOnVMStart;
    VirtualMachine vm;
    private boolean vmDied = false;

    public MyThread(VirtualMachine vm, boolean stopOnVMStart, String pkgName, int numClasses, GUI_Coverage_Tool inputFrame) {
        this.vm = vm;
        this.stopOnVMStart = stopOnVMStart;
        this.start();
        this.numClasses = numClasses;
        this.pkgName = pkgName;
        this.mainFrame = inputFrame;
    }

    private boolean breakpointEvent(Event event) {
        BreakpointEvent be = (BreakpointEvent)event;
        if (this.ht.containsKey(be.location().method().toString())) {
            Integer i = (Integer)this.ht.get(be.location().method().toString());
            int j = i + 1;
            this.ht.put(be.location().method().toString(), Integer.valueOf(j));
        } else {
            this.ht.put(be.location().method().toString(), Integer.valueOf(1));
        }

        this.mainFrame.updateNumbers();
        return false;
    }

    private boolean classPrepareEvent(Event event) {
        ClassPrepareEvent cle = (ClassPrepareEvent)event;
        return false;
    }

    private boolean classUnloadEvent(Event event) {
        ClassUnloadEvent cue = (ClassUnloadEvent)event;
        return false;
    }

    private boolean exceptionEvent(Event event) {
        ExceptionEvent ee = (ExceptionEvent)event;
        return true;
    }

    private boolean fieldWatchEvent(Event event) {
        WatchpointEvent fwe = (WatchpointEvent)event;
        return true;
    }

    public Hashtable getHashTable() {
        return this.ht;
    }

    private boolean handleEvent(Event event) {
        if (event instanceof ExceptionEvent) {
            return this.exceptionEvent(event);
        } else if (event instanceof BreakpointEvent) {
            return this.breakpointEvent(event);
        } else if (event instanceof WatchpointEvent) {
            return this.fieldWatchEvent(event);
        } else if (event instanceof StepEvent) {
            return this.stepEvent(event);
        } else if (event instanceof MethodEntryEvent) {
            return this.methodEntryEvent(event);
        } else if (event instanceof MethodExitEvent) {
            return this.methodExitEvent(event);
        } else if (event instanceof ClassPrepareEvent) {
            ClassPrepareEvent cpe = (ClassPrepareEvent)event;
            this.rt = cpe.referenceType();

            try {
                List l = this.rt.methods();
                Object[] o = l.toArray();

                for(int i = 0; i < o.length; ++i) {
                    Method met = (Method)o[i];
                    Location loc = met.location();
                    BreakpointRequest br = this.vm.eventRequestManager().createBreakpointRequest(loc);
                    br.enable();
                }
            } catch (Exception var9) {
                System.out.println(var9);
            }

            return this.classPrepareEvent(event);
        } else if (event instanceof ClassUnloadEvent) {
            return this.classUnloadEvent(event);
        } else if (event instanceof ThreadStartEvent) {
            return this.threadStartEvent(event);
        } else if (event instanceof ThreadDeathEvent) {
            return this.threadDeathEvent(event);
        } else {
            return event instanceof VMStartEvent ? this.vmStartEvent(event) : this.handleExitEvent(event);
        }
    }

    private boolean handleExitEvent(Event event) {
        if (event instanceof VMDeathEvent) {
            this.vmDied = true;
            return this.vmDeathEvent(event);
        } else if (event instanceof VMDisconnectEvent) {
            this.connected = false;
            if (!this.vmDied) {
                this.vmDisconnectEvent(event);
            }

            return false;
        } else {
            throw new InternalError("Unexpected event type");
        }
    }

    private boolean methodEntryEvent(Event event) {
        MethodEntryEvent me = (MethodEntryEvent)event;
        System.out.println("MethodEntryEvent");
        System.out.println(me.method().toString());
        System.out.println(me.location().lineNumber());
        return true;
    }

    private boolean methodExitEvent(Event event) {
        MethodExitEvent me = (MethodExitEvent)event;
        return true;
    }

    public void run() {
        EventQueue queue = this.vm.eventQueue();

        for(int i = 1; i <= this.numClasses; ++i) {
            ClassPrepareRequest cpr = this.vm.eventRequestManager().createClassPrepareRequest();
            cpr.addClassFilter(this.pkgName + ".*");
            cpr.addCountFilter(i);
            cpr.enable();
        }

        while(this.connected) {
            try {
                EventSet eventSet = queue.remove();
                boolean resumeStoppedApp = false;

                Event e;
                for(EventIterator it = eventSet.eventIterator(); it.hasNext(); resumeStoppedApp |= !this.handleEvent(e)) {
                    e = it.nextEvent();
                }

                if (resumeStoppedApp) {
                    eventSet.resume();
                }
            } catch (InterruptedException var7) {
            } catch (VMDisconnectedException var8) {
                break;
            }
        }

    }

    private boolean stepEvent(Event event) {
        StepEvent se = (StepEvent)event;
        return true;
    }

    private boolean threadDeathEvent(Event event) {
        ThreadDeathEvent tee = (ThreadDeathEvent)event;
        return false;
    }

    private boolean threadStartEvent(Event event) {
        ThreadStartEvent tse = (ThreadStartEvent)event;
        return false;
    }

    public boolean vmDeathEvent(Event event) {
        return false;
    }

    public boolean vmDisconnectEvent(Event event) {
        System.out.println("VMDisconnectEvent");
        return false;
    }

    private boolean vmStartEvent(Event event) {
        VMStartEvent se = (VMStartEvent)event;
        return this.stopOnVMStart;
    }
}
