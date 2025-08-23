# Memory Visualizer - File Tree Structure

```
Memoryvisualizer_CSE323/
├── README.md                                   # Project documentation
├── LICENSE                                     # MIT License
├── .gitignore                                  # Git ignore rules
├── gradle.properties                           # Gradle configuration
├── settings.gradle.kts                         # Gradle settings
├── build.gradle.kts                            # Root build configuration
├── gradlew                                     # Gradle wrapper (Unix)
├── gradlew.bat                                # Gradle wrapper (Windows)
├── local.properties                           # Local SDK paths
├── gradle/
│   ├── libs.versions.toml                     # Version catalog
│   └── wrapper/
│       ├── gradle-wrapper.jar                 # Gradle wrapper JAR
│       └── gradle-wrapper.properties          # Wrapper configuration
└── app/
    ├── build.gradle.kts                       # App build configuration
    ├── proguard-rules.pro                     # ProGuard rules
    ├── src/
    │   ├── androidTest/java/                  # Android instrumentation tests
    │   ├── test/java/                         # Unit tests
    │   └── main/
    │       ├── AndroidManifest.xml            # App manifest
    │       ├── java/com/example/memoryvisualizer/
    │       │   ├── model/                     # 🧠 BUSINESS LOGIC LAYER
    │       │   │   ├── AllocationResult.kt    # Simulation state snapshot
    │       │   │   │   ├── data class AllocationResult
    │       │   │   │   ├── freeBlocks, allocatedBlocks properties
    │       │   │   │   ├── waitingProcesses, allocatedProcesses
    │       │   │   │   ├── successPercentage calculation
    │       │   │   │   └── memoryUtilization metrics
    │       │   │   │
    │       │   │   ├── CompactionManager.kt   # Memory compaction operations
    │       │   │   │   ├── class CompactionManager
    │       │   │   │   ├── compact() - main compaction logic
    │       │   │   │   ├── compactWithValidation() - enhanced version
    │       │   │   │   ├── isAlreadyCompacted() - optimization check
    │       │   │   │   └── calculateFragmentation() - metrics
    │       │   │   │
    │       │   │   ├── FragmentationStatus.kt # Fragmentation metrics
    │       │   │   │   ├── data class FragmentationStats
    │       │   │   │   ├── internalTotal, externalTotal
    │       │   │   │   ├── largestFree, holeCount
    │       │   │   │   ├── calculateSuccessPercentage()
    │       │   │   │   └── getMemoryUtilization()
    │       │   │   │
    │       │   │   ├── MemoryBlock.kt          # Memory block representation
    │       │   │   │   ├── data class MemoryBlock
    │       │   │   │   ├── id, start, size, isFree properties
    │       │   │   │   ├── end property (calculated)
    │       │   │   │   ├── getProcessId() - extract from block ID
    │       │   │   │   ├── isAdjacentTo() - adjacency checking
    │       │   │   │   ├── canFit() - size validation
    │       │   │   │   └── withFreeStatus() - status modification
    │       │   │   │
    │       │   │   ├── Process.kt              # Process definition with timing
    │       │   │   │   ├── enum class ProcessStatus
    │       │   │   │   │   └── ALLOCATED, WAITING, FAILED, COMPLETED
    │       │   │   │   ├── data class ProcessDef
    │       │   │   │   │   ├── id, size, status properties
    │       │   │   │   │   ├── arrivalTime, burstTime, remainingBurst
    │       │   │   │   │   └── allocatedBlockId
    │       │   │   │   ├── isAllocated, isWaiting, hasFailed properties
    │       │   │   │   ├── isCompleted property
    │       │   │   │   ├── hasArrived(currentTime) - time checking
    │       │   │   │   ├── shouldAutoFree() - burst time logic
    │       │   │   │   ├── withStatus() - status updates
    │       │   │   │   ├── allocatedTo() - allocation
    │       │   │   │   ├── markCompleted() - completion
    │       │   │   │   └── withRemainingBurst() - time updates
    │       │   │   │
    │       │   │   ├── SimulationEngine.kt     # 🚀 CORE SIMULATION LOGIC
    │       │   │   │   ├── class SimulationEngine (internal)
    │       │   │   │   ├── State Management:
    │       │   │   │   │   ├── blocks: MutableList<MemoryBlock>
    │       │   │   │   │   ├── processes: MutableList<ProcessDef>
    │       │   │   │   │   ├── currentTime: Int
    │       │   │   │   │   ├── allocationTimes: Map<String, Int>
    │       │   │   │   │   └── nextProcessIdx: Int
    │       │   │   │   ├── History Management:
    │       │   │   │   │   ├── snapshots: List<AllocationResult>
    │       │   │   │   │   ├── timeSnapshots: List<Int>
    │       │   │   │   │   ├── allocationTimeSnapshots: List<Map>
    │       │   │   │   │   └── cursor: Int
    │       │   │   │   ├── Core Methods:
    │       │   │   │   │   ├── load() - initialize simulation
    │       │   │   │   │   ├── load() - overloaded with timing
    │       │   │   │   │   ├── step() - single allocation step
    │       │   │   │   │   ├── runAll() - complete simulation
    │       │   │   │   │   ├── compact() - memory compaction
    │       │   │   │   │   ├── reset() - restore initial state
    │       │   │   │   │   ├── current() - get current state
    │       │   │   │   │   ├── undo() - previous state
    │       │   │   │   │   └── redo() - next state
    │       │   │   │   ├── Time-Based Logic:
    │       │   │   │   │   ├── handleBurstCompletions() - auto-free
    │       │   │   │   │   ├── findNextAvailableProcess() - SJF scheduling
    │       │   │   │   │   └── findNextArrivalTime() - time advancement
    │       │   │   │   ├── Memory Management:
    │       │   │   │   │   ├── splitAndAllocate() - block splitting
    │       │   │   │   │   ├── coalesceFree() - merge adjacent blocks
    │       │   │   │   │   └── recomputeStats() - fragmentation metrics
    │       │   │   │   └── Snapshot System:
    │       │   │   │       ├── snapshot() - create state snapshot
    │       │   │   │       ├── saveSnapshot() - persist state
    │       │   │   │       └── restoreFromSnapshot() - restore state
    │       │   │   │
    │       │   │   ├── Simulator.kt            # Public interface
    │       │   │   │   ├── interface Simulator
    │       │   │   │   ├── load() methods (simple & advanced)
    │       │   │   │   ├── setStrategy() - algorithm selection
    │       │   │   │   ├── step(), runAll() - execution control
    │       │   │   │   ├── compact(), reset() - state management
    │       │   │   │   ├── current() - state access
    │       │   │   │   └── undo(), redo(), canUndo(), canRedo()
    │       │   │   │
    │       │   │   ├── SimulatorImpl.kt        # Interface implementation
    │       │   │   │   ├── class SimulatorImpl
    │       │   │   │   ├── engine: SimulationEngine (delegation)
    │       │   │   │   ├── strategy: AllocationStrategy
    │       │   │   │   └── All interface method implementations
    │       │   │   │
    │       │   │   └── strategy/               # 🎯 ALLOCATION STRATEGIES
    │       │   │       ├── AllocationStrategy.kt  # Strategy interface
    │       │   │       │   ├── interface AllocationStrategy
    │       │   │       │   ├── chooseBlock() - core selection method
    │       │   │       │   └── name property
    │       │   │       │
    │       │   │       ├── BestFitStrategy.kt    # Best fit implementation
    │       │   │       │   ├── class BestFitStrategy
    │       │   │       │   ├── chooseBlock() - smallest suitable block
    │       │   │       │   ├── Tie-breaking by lower address
    │       │   │       │   └── Time complexity: O(n)
    │       │   │       │
    │       │   │       ├── FirstFitStrategy.kt   # First fit implementation
    │       │   │       │   ├── class FirstFitStrategy
    │       │   │       │   ├── chooseBlock() - first suitable block
    │       │   │       │   ├── Natural address order
    │       │   │       │   └── Time complexity: O(n)
    │       │   │       │
    │       │   │       └── WorstFitStrategy.kt   # Worst fit implementation
    │       │   │           ├── class WorstFitStrategy
    │       │   │           ├── chooseBlock() - largest suitable block
    │       │   │           ├── Tie-breaking by lower address
    │       │   │           └── Time complexity: O(n)
    │       │   │
    │       │   ├── stub/                       # 🔌 DATA ADAPTATION LAYER
    │       │   │   └── SimulatorStub.kt        # UI-friendly adapter
    │       │   │       ├── class SimulatorStub
    │       │   │       ├── Data Classes:
    │       │   │       │   ├── BlockStub - UI-friendly MemoryBlock
    │       │   │       │   ├── ProcessStub - UI-friendly ProcessDef
    │       │   │       │   ├── StatsStub - UI-friendly FragmentationStats
    │       │   │       │   └── AllocationResultStub - complete state
    │       │   │       ├── Strategy enum (FIRST, BEST, WORST)
    │       │   │       ├── realSimulator: SimulatorImpl (delegation)
    │       │   │       ├── All public simulation methods
    │       │   │       └── Utility methods for UI calculations
    │       │   │
    │       │   └── ui/                         # 🎨 USER INTERFACE LAYER
    │       │       ├── activity/               # Main activities
    │       │       │   ├── MainActivity.kt     # Single activity architecture
    │       │       │   │   ├── class MainActivity
    │       │       │   │   ├── ViewPager2 setup
    │       │       │   │   ├── Fragment management
    │       │       │   │   └── AppTopBar integration
    │       │       │   │
    │       │       │   └── MainPagerAdapter.kt # ViewPager adapter
    │       │       │       ├── class MainPagerAdapter
    │       │       │       ├── Fragment creation (Input, Visualization)
    │       │       │       └── Page titles management
    │       │       │
    │       │       ├── components/             # Reusable UI components
    │       │       │   ├── AppTopBar.kt        # Top app bar component
    │       │       │   ├── Buttons.kt          # Custom button components
    │       │       │   ├── SectionCard.kt      # Card layout component
    │       │       │   └── StatusChip.kt       # Status indicator chips
    │       │       │
    │       │       ├── fragment/               # Screen fragments
    │       │       │   ├── InputFragment.kt    # 📝 Input configuration screen
    │       │       │   │   ├── class InputFragment
    │       │       │   │   ├── UI Components:
    │       │       │   │   │   ├── Memory blocks input
    │       │       │   │   │   ├── Process sizes input
    │       │       │   │   │   ├── Strategy selection dropdown
    │       │       │   │   │   ├── Advanced options toggle
    │       │       │   │   │   ├── Arrival times input
    │       │       │   │   │   ├── Burst times input
    │       │       │   │   │   ├── Load button
    │       │       │   │   │   └── Error display card
    │       │       │   │   ├── Input Validation:
    │       │       │   │   │   ├── validateNow() - comprehensive validation
    │       │       │   │   │   ├── CSV parsing with error handling
    │       │       │   │   │   ├── Array length matching
    │       │       │   │   │   └── Real-time error clearing
    │       │       │   │   ├── CSV Parsing:
    │       │       │   │   │   ├── parseCsv() - basic positive integers
    │       │       │   │   │   ├── parseCsvAllowEmpty() - arrival times
    │       │       │   │   │   └── parseCsvNullable() - burst times
    │       │       │   │   └── Advanced Mode:
    │       │       │   │       ├── SwitchMaterial toggle
    │       │       │   │       ├── Container visibility management
    │       │       │   │       └── Helper text with examples
    │       │       │   │
    │       │       │   └── VisualizationFragment.kt # 📊 Simulation display screen
    │       │       │       ├── class VisualizationFragment
    │       │       │       ├── UI Components:
    │       │       │       │   ├── Control panel (Step, Run, Compact, Reset)
    │       │       │       │   ├── Undo/Redo buttons
    │       │       │       │   ├── Action display text
    │       │       │       │   ├── Statistics display
    │       │       │       │   ├── Memory canvas view
    │       │       │       │   └── Empty state overlay
    │       │       │       ├── User Interactions:
    │       │       │       │   ├── Button press animations
    │       │       │       │   ├── Block click handlers
    │       │       │       │   ├── Block long press (info sheet)
    │       │       │       │   └── Accessibility support
    │       │       │       ├── State Management:
    │       │       │       │   ├── Control button states
    │       │       │       │   ├── Undo/Redo availability
    │       │       │       │   └── Data loading states
    │       │       │       └── Information Display:
    │       │       │           ├── Block info bottom sheet
    │       │       │           ├── Process allocation details
    │       │       │           └── Snackbar notifications
    │       │       │
    │       │       ├── screens/                # Compose screens (future)
    │       │       │   ├── SetupScreen.kt      # Compose setup screen
    │       │       │   └── VisualizeScreen.kt  # Compose visualization screen
    │       │       │
    │       │       ├── theme/                  # App theming
    │       │       │   └── Theme.kt            # Material Design 3 theme
    │       │       │
    │       │       ├── util/                   # UI utilities
    │       │       │   └── ColorPalette.kt     # Process color management
    │       │       │       ├── object ColorPalette
    │       │       │       ├── colorForProcess() - consistent coloring
    │       │       │       ├── Process ID to color mapping
    │       │       │       └── Material color palette
    │       │       │
    │       │       ├── view/                   # Custom views
    │       │       │   ├── MemoryCanvasView.kt # 🎨 Memory visualization canvas
    │       │       │   │   ├── class MemoryCanvasView : View
    │       │       │   │   ├── Rendering:
    │       │       │   │   │   ├── onDraw() - main drawing logic
    │       │       │   │   │   ├── Block drawing with proper scaling
    │       │       │   │   │   ├── Process labels and IDs
    │       │       │   │   │   ├── Touch handling for interactions
    │       │       │   │   │   └── Zoom and pan support
    │       │       │   │   ├── Interaction Handling:
    │       │       │   │   │   ├── onTouchEvent() - touch processing
    │       │       │   │   │   ├── Block click detection
    │       │       │   │   │   ├── Long press support
    │       │       │   │   │   └── Gesture recognition
    │       │       │   │   ├── Visual Features:
    │       │       │   │   │   ├── Block highlighting
    │       │       │   │   │   ├── Selection indicators
    │       │       │   │   │   ├── Animation support
    │       │       │   │   │   └── Responsive text sizing
    │       │       │   │   └── Callbacks:
    │       │       │   │       ├── onBlockClick lambda
    │       │       │   │       └── onBlockLongPress lambda
    │       │       │   │
    │       │       │   └── RenderBlockMapper.kt # Block data transformation
    │       │       │       ├── object RenderBlockMapper
    │       │       │       ├── data class RenderBlock
    │       │       │       │   ├── id, start, size, isFree
    │       │       │       │   ├── processId, internalFrag
    │       │       │       │   └── color property
    │       │       │       └── map() - stub to render conversion
    │       │       │
    │       │       └── viewmodel/              # View models
    │       │           └── VisualizerViewModel.kt # 🔄 Main view model
    │       │               ├── class VisualizerViewModel : ViewModel
    │       │               ├── Dependencies:
    │       │               │   └── sim: SimulatorStub
    │       │               ├── State Management:
    │       │               │   ├── _state: MutableStateFlow<AllocationResultStub?>
    │       │               │   ├── state: StateFlow (public)
    │       │               │   ├── _errors: MutableSharedFlow<String>
    │       │               │   ├── errors: SharedFlow (public)
    │       │               │   ├── _loaded: MutableSharedFlow<Unit>
    │       │               │   └── loaded: SharedFlow (public)
    │       │               ├── Public Methods:
    │       │               │   ├── onLoad() - simple input loading
    │       │               │   ├── onLoad() - advanced input loading
    │       │               │   ├── onStrategySelected() - algorithm selection
    │       │               │   ├── onStep() - single step execution
    │       │               │   ├── onRun() - complete execution
    │       │               │   ├── onCompact() - memory compaction
    │       │               │   ├── onReset() - reset simulation
    │       │               │   ├── onUndo() - undo last action
    │       │               │   ├── onRedo() - redo action
    │       │               │   ├── canUndo() - undo availability
    │       │               │   └── canRedo() - redo availability
    │       │               ├── Input Processing:
    │       │               │   ├── parseCsv() - basic CSV parsing
    │       │               │   ├── parseCsvAllowEmpty() - arrival time parsing
    │       │               │   ├── parseCsvNullable() - burst time parsing
    │       │               │   └── Input validation and error emission
    │       │               └── Private Methods:
    │       │                   ├── update() - state update helper
    │       │                   └── emitError() - error handling
    │       │
    │       └── res/                            # 📱 ANDROID RESOURCES
    │           ├── layout/                     # XML layouts
    │           │   ├── activity_main.xml       # Main activity layout
    │           │   ├── fragment_input.xml      # Input screen layout
    │           │   │   ├── ScrollView container
    │           │   │   ├── Memory blocks card
    │           │   │   ├── Process queue card
    │           │   │   ├── Strategy selection card
    │           │   │   ├── Advanced options card
    │           │   │   │   ├── Toggle switch
    │           │   │   │   ├── Arrival times input
    │           │   │   │   └── Burst times input
    │           │   │   ├── Error message card
    │           │   │   └── Load button (bottom-anchored)
    │           │   ├── fragment_visualization.xml # Visualization screen layout
    │           │   │   ├── ScrollView container
    │           │   │   ├── Control panel card
    │           │   │   │   ├── Primary controls (Step, Run, Compact)
    │           │   │   │   └── Secondary controls (Reset, Undo, Redo)
    │           │   │   ├── Status panel card
    │           │   │   │   ├── Last action display
    │           │   │   │   └── Statistics display
    │           │   │   ├── Visualization card
    │           │   │   │   ├── Memory visualization header
    │           │   │   │   ├── MemoryCanvasView
    │           │   │   │   └── Empty state overlay
    │           │   └── bottom_sheet_block_info.xml # Block info sheet
    │           │       ├── Block details display
    │           │       ├── Process information
    │           │       └── Close button
    │           │
    │           ├── values/                     # Resource values
    │           │   ├── colors.xml              # Color definitions
    │           │   │   ├── Scandi color palette
    │           │   │   ├── Background colors
    │           │   │   ├── Surface colors
    │           │   │   ├── Text colors
    │           │   │   ├── Border colors
    │           │   │   └── Error colors
    │           │   ├── strings.xml             # String resources
    │           │   │   ├── App name and titles
    │           │   │   ├── Input labels and hints
    │           │   │   ├── Helper text messages
    │           │   │   ├── Button labels
    │           │   │   ├── Error messages
    │           │   │   ├── Action descriptions
    │           │   │   └── Accessibility strings
    │           │   ├── dimens.xml              # Dimension resources
    │           │   │   ├── Text sizes
    │           │   │   ├── Spacing values
    │           │   │   ├── Card dimensions
    │           │   │   ├── Button heights
    │           │   │   ├── Icon sizes
    │           │   │   └── Border radii
    │           │   ├── styles.xml              # Style definitions
    │           │   │   ├── Text styles
    │           │   │   ├── Button styles
    │           │   │   ├── Input field styles
    │           │   │   └── Card styles
    │           │   └── themes.xml              # App themes
    │           │       ├── Material Design 3 theme
    │           │       ├── Color scheme
    │           │       └── Component styling
    │           │
    │           ├── drawable/                   # Graphics and icons
    │           │   ├── ic_launcher.xml         # App launcher icon
    │           │   ├── ic_play.xml             # Play/Load icon
    │           │   ├── ic_step.xml             # Step icon
    │           │   ├── ic_run.xml              # Run icon
    │           │   ├── ic_compact.xml          # Compact icon
    │           │   ├── ic_reset.xml            # Reset icon
    │           │   ├── ic_undo.xml             # Undo icon
    │           │   ├── ic_redo.xml             # Redo icon
    │           │   ├── ic_error.xml            # Error icon
    │           │   └── ic_info.xml             # Information icon
    │           │
    │           └── anim/                       # Animations
    │               ├── button_press.xml        # Button press animation
    │               ├── slide_in_bottom.xml     # Bottom slide animation
    │               └── fade_in.xml             # Fade in animation
    │
    └── build/                                  # Build outputs (generated)
        ├── generated/                          # Generated code
        ├── intermediates/                      # Intermediate build files
        ├── kotlin/                             # Compiled Kotlin
        ├── outputs/                            # Final outputs (APKs)
        └── tmp/                                # Temporary files
```

## 📋 Key Features by Layer

### 🧠 Model Layer (Business Logic)
- **Core Algorithms**: First Fit, Best Fit, Worst Fit allocation strategies
- **Time Management**: Arrival times, burst times, SJF scheduling
- **Memory Management**: Dynamic allocation, compaction, fragmentation tracking
- **State Management**: Undo/redo, snapshots, history preservation

### 🔌 Stub Layer (Data Adaptation)
- **UI Compatibility**: Transforms model data for UI consumption
- **Backward Compatibility**: Maintains API stability
- **Type Safety**: Provides UI-specific data types

### 🎨 UI Layer (User Interface)
- **Material Design 3**: Modern Android design guidelines
- **Interactive Visualization**: Touch-enabled memory canvas
- **Advanced Input**: Toggle between simple and complex configurations
- **Real-time Feedback**: Live validation and error display

## 🔧 Architecture Highlights

- **MVVM Pattern**: Clear separation of concerns
- **Strategy Pattern**: Pluggable allocation algorithms
- **Observer Pattern**: Reactive UI updates with StateFlow
- **Command Pattern**: Undo/redo functionality
- **Facade Pattern**: SimulatorStub simplifies complex model interactions

## 📱 UI/UX Features

- **Responsive Design**: Adapts to different screen sizes
- **Accessibility**: Full accessibility support with content descriptions
- **Progressive Disclosure**: Advanced options hidden by default
- **Visual Feedback**: Animations and state indicators
- **Error Prevention**: Real-time validation and helpful error messages
