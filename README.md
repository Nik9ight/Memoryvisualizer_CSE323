# Memory Visualizer

A sophisticated Android application for visualizing and simulating memory allocation algorithms with time-based scheduling support.

## 🎯 Features

### Core Memory Allocation
- **Multiple Allocation Strategies**: First Fit, Best Fit, Worst Fit
- **Dynamic Memory Management**: Variable-sized block allocation and deallocation
- **Memory Compaction**: Consolidate free space to reduce external fragmentation
- **Real-time Visualization**: Interactive memory layout display

### Advanced Scheduling
- **Arrival Time Support**: Processes arrive at specific times
- **Burst Time Management**: Automatic memory deallocation after execution
- **Shortest Job First (SJF)**: Tie-breaking for processes with same arrival time
- **Time-based Simulation**: Step-by-step or complete execution modes

### User Experience
- **Interactive UI**: Material Design 3 components
- **Undo/Redo Support**: Navigate through simulation history
- **Advanced Input Options**: Toggle between simple and advanced configurations
- **Real-time Statistics**: Fragmentation metrics and success rates

## 📱 Screenshots

*[Add screenshots here showing the app in action]*

## 🏗️ Architecture

### Model-View-ViewModel (MVVM)
```
├── Model Layer (Business Logic)
├── View Layer (UI Components)
└── ViewModel Layer (Data Binding)
```

### Key Components

#### **Simulation Engine**
- Core allocation algorithms
- Time-based process scheduling
- Memory block management
- State snapshot system

#### **Strategy Pattern**
- Pluggable allocation algorithms
- Clean separation of concerns
- Easy to extend with new strategies

#### **Stub Pattern**
- UI-friendly data adaptation
- Backward compatibility
- Clean API boundaries

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 21+
- Kotlin 1.8+

### Installation
1. Clone the repository
```bash
git clone https://github.com/Nik9ight/Memoryvisualizer_CSE323.git
```

2. Open in Android Studio
3. Sync Gradle files
4. Run the app

### Basic Usage

#### Simple Mode
1. Enter memory block sizes: `100, 200, 150, 300`
2. Enter process sizes: `50, 80, 120, 60`
3. Select allocation strategy
4. Click "Load" and start simulation

#### Advanced Mode
1. Toggle "Advanced Options"
2. Add arrival times: `0, 2, 4, 6`
3. Add burst times: `3, 5, 2, 4`
4. Run time-based simulation

## 📚 Memory Allocation Algorithms

### First Fit
- Allocates to the first block that can accommodate the process
- Fast allocation time: O(n)
- May cause external fragmentation

### Best Fit
- Allocates to the smallest block that can fit the process
- Minimizes wasted space
- Slower allocation: O(n log n)

### Worst Fit
- Allocates to the largest available block
- Leaves larger remaining blocks
- Can reduce external fragmentation

## 🕒 Time-Based Features

### Arrival Times
- Processes become eligible for allocation at specific times
- Simulation advances time automatically
- Realistic process scheduling

### Burst Times
- Automatic memory deallocation after execution
- Supports long-running and short processes
- `null` burst time = never auto-free

### SJF Tie-Breaking
When multiple processes arrive simultaneously:
1. **Priority 1**: Shortest burst time
2. **Priority 2**: Original process order
3. **Special case**: Processes with burst time over those without

## 🧮 Fragmentation Metrics

- **Internal Fragmentation**: Wasted space within allocated blocks
- **External Fragmentation**: Free space between allocated blocks
- **Success Percentage**: Successfully allocated processes
- **Memory Utilization**: Percentage of total memory in use

## 📂 Project Structure

```
app/src/main/java/com/example/memoryvisualizer/
├── model/                          # Business Logic Layer
│   ├── AllocationResult.kt         # Simulation state snapshot
│   ├── CompactionManager.kt        # Memory compaction operations
│   ├── FragmentationStatus.kt     # Fragmentation metrics
│   ├── MemoryBlock.kt             # Memory block representation
│   ├── Process.kt                 # Process definition with timing
│   ├── SimulationEngine.kt        # Core simulation logic
│   ├── Simulator.kt               # Public interface
│   ├── SimulatorImpl.kt           # Interface implementation
│   └── strategy/                  # Allocation Strategies
│       ├── AllocationStrategy.kt   # Strategy interface
│       ├── BestFitStrategy.kt     # Best fit implementation
│       ├── FirstFitStrategy.kt    # First fit implementation
│       └── WorstFitStrategy.kt    # Worst fit implementation
├── stub/                          # Data Adaptation Layer
│   └── SimulatorStub.kt          # UI-friendly data adapter
├── ui/                           # User Interface Layer
│   ├── activity/                 # Main activities
│   ├── components/               # Reusable UI components
│   ├── fragment/                 # Screen fragments
│   ├── screens/                  # Compose screens
│   ├── theme/                    # App theming
│   ├── util/                     # UI utilities
│   ├── view/                     # Custom views
│   └── viewmodel/                # View models
└── res/                          # Resources
    ├── layout/                   # XML layouts
    ├── values/                   # Colors, strings, dimensions
    └── drawable/                 # Icons and graphics
```

## 🔧 Technical Details

### Memory Block Management
```kotlin
data class MemoryBlock(
    val id: String,
    val start: Int,
    val size: Int,
    val isFree: Boolean
)
```

### Process Definition
```kotlin
data class ProcessDef(
    val id: String,
    val size: Int,
    val status: ProcessStatus,
    val arrivalTime: Int = 0,
    val burstTime: Int? = null
)
```

### Time-Based Simulation
- **Current Time Tracking**: Maintains simulation clock
- **Event Scheduling**: Handles arrivals and completions
- **State Management**: Undo/redo with time state preservation

## 🧪 Testing

### Unit Tests
```bash
./gradlew test
```

### Integration Tests
```bash
./gradlew connectedAndroidTest
```

### Manual Testing Scenarios
1. **Basic Allocation**: Test all three strategies
2. **Time-Based Simulation**: Verify arrival and burst times
3. **Edge Cases**: Empty inputs, large processes, zero burst times
4. **UI Interactions**: Toggle advanced mode, undo/redo operations

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Kotlin coding conventions
- Write unit tests for new features
- Update documentation for API changes
- Use meaningful commit messages

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Authors

- **Nik9ight** - *Initial work* - [GitHub](https://github.com/Nik9ight)

## 🙏 Acknowledgments

- Computer Science Education community
- Operating Systems textbook examples
- Material Design 3 guidelines
- Android Architecture Components

## 🐛 Known Issues

- Large datasets (>1000 processes) may cause UI lag
- Very small burst times (1-2 units) might not be visually distinct

## 🗺️ Roadmap

- [ ] Export simulation results to CSV
- [ ] Additional allocation strategies (Next Fit, Buddy System)
- [ ] Process priority levels
- [ ] Multi-level queue scheduling
- [ ] 3D memory visualization
- [ ] Performance benchmarking tools

## 📞 Support

For support, create an issue on GitHub.

---

**Built with ❤️ for Computer Science Education**
