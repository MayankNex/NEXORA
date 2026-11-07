# NEXORA AI — Personal AI Intelligence Platform

NEXORA AI is an Android-native, user-controlled AI intelligence platform engineered for developers, researchers, and power users. It delivers real-time streaming conversations, custom AI personalities, and an adaptable multi-provider API engine supporting OpenAI, Groq, DeepSeek, OpenRouter, and local Ollama or self-hosted endpoints with zero forced restrictions on harmless educational or cybersecurity discussions.

***

## User Review & Critical Decisions

> [!IMPORTANT]
> NEXORA AI provides native, client-side freedom: your API keys, custom endpoints, model IDs, and conversation histories are stored privately on your device. The questions presented earlier were dismissed without answers, so we have adopted optimal, privacy-preserving defaults that satisfy all prompt requirements:

- **Confirmed Decision 1: Direct Universal OpenAI-Compatible Client**: NEXORA connects directly from Android to any standard OpenAI-compatible API endpoint (OpenAI, Groq, DeepSeek, OpenRouter, Ollama, vLLM, LM Studio, or custom self-hosted proxy) via OkHttp Server-Sent Events (SSE) streaming.
- **Confirmed Decision 2: Zero-Leak Local Storage via Room**: Chat sessions, message history, token metrics, custom provider credentials, and system personalities are persisted locally in an on-device SQLite/Room database with one-click JSON backup, export, and complete credential wiping.
- **Confirmed Decision 3: Preloaded Multi-Persona Suite**: Out of the box, NEXORA includes 4 specialized AI personas:
  1. *Nexora Prime* (balanced, concise, proactive intelligence)
  2. *Defensive CyberSec & AppSec Researcher* (unfiltered technical security analysis, vulnerability triage, and code auditing)
  3. *Full-Stack Code Architect* (clean code, syntax-highlighted generation, refactoring, and test cases)
  4. *Hinglish / Multilingual Companion* (seamless conversational flow in natural Hinglish, Hindi, and English)

***

## 1. Overview & Core Concept

- **What It Does**: A sovereign AI workspace on Android that liberates users from walled-garden chat interfaces. Users can register unlimited AI providers, paste API keys, configure custom base URLs and model IDs, test connections, tune sampling parameters (temperature, max tokens, top-p), and stream responses into a futuristic, dark purple/charcoal interface with syntax-highlighted code blocks, conversation branching, and token analytics.
- **Target Audience / Persona**: Developers, security analysts, researchers, and enthusiasts who want full control over their models, custom system prompts, and data privacy without artificial refusals on benign technical or fictional topics.
- **Key Value**: Bring-your-own-provider flexibility, offline-first chat history, zero cloud telemetry, deep customization, and seamless streaming with full markdown and code export tools.

***

## 2. User Experience & Visual Design

### Key User Flows
1. **Onboarding & Quick Setup**:
   - First launch displays the futuristic NEXORA welcome screen with pre-configured provider templates (Groq, OpenAI, DeepSeek, OpenRouter, and Ollama / Local).
   - Instant "Test Connection" button sends a lightweight ping to verify base URL, model availability, and API key validity with clear error badges (quota exceeded, 401 unauthorized, rate-limited, or host unreachable).
2. **Conversation & Real-Time Streaming**:
   - ChatGPT-inspired clean chat view with message bubbles, user avatar, and pulsating AI streaming cursor.
   - Live token-by-token streaming via SSE. Stop generation at any time with a dedicated floating cancel button.
   - Code blocks with language badge, monospaced font, and a 1-tap "Copy Code" action.
   - Message action toolbar: Copy text, Edit & Resubmit, Regenerate response, or Delete message.
3. **Sidebar & Session Management**:
   - Smooth navigation drawer / sheet with searchable chat histories, grouped by Today, Yesterday, and Older.
   - Quick session actions: Rename session, Pin to top, Export conversation (Markdown/JSON), and Delete.
4. **Persona & Prompt Studio**:
   - Switch active assistant personality on the fly via a top-bar pill selector.
   - Customize system instructions, tone (Professional, Analytical, Direct, Creative, Hinglish), and temperature slider.
5. **Settings & Token Telemetry**:
   - Provider manager: Add/edit custom endpoints, set default models, test latencies.
   - Analytics dashboard: Prompt tokens, completion tokens, estimated cost, and session counts.
   - Secure Key Wipe: Instant one-tap button to purge all stored keys and history.

### Visual Identity & Theme
- **Aesthetic Direction**: *Cyberpunk Luxury & Dark Charcoal Minimalism*. High contrast, glowing violet/electric purple accents, refined typography, subtle glassmorphic card borders, and smooth spring animations.
- **Color Palette**:
  - Dominant Background: Deep Charcoal Black (`#0D0E15`)
  - Surface Container / Chat Bubble (AI): Dark Obsidian Slate (`#161822`)
  - User Chat Bubble: Electric Violet Gradient / Royal Purple (`#6C38F7` to `#8B5CF6`)
  - Primary Accent & Glow: Neon Violet (`#9D71FD`)
  - Secondary Accent / CyberSec: Cyan Mint (`#06D6A0`)
  - Error / Alert: Crimson Coral (`#EF476F`)
  - Text Primary: Crisp High-White (`#F3F4F6`), Text Secondary: Muted Silver (`#9CA3AF`)
- **Typography & Hierarchy**:
  - Monospaced code blocks and token counters for technical precision.
  - Clean M3 typography scale with bold display headers and comfortable 16sp body text with 1.4 line-height for readability.
- **Interactive Feedback & Motion**:
  - Pulsing glow during streaming generation.
  - Smooth spring transitions between sessions and drawer opening.
  - Haptic feedback and ripple indications on all interactive buttons.

***

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Direct SSE Streaming via OkHttp vs Server Proxy**:
  - *Chosen Approach*: Direct OkHttp client with `EventSourceListener` on Android.
  - *Why*: Eliminates the need for a secondary backend server, allowing users to run NEXORA completely standalone on their phone, connect directly to cloud APIs (Groq, OpenAI, OpenRouter) or local network Ollama servers (`http://192.168.x.x:11434/v1`), maintaining absolute privacy.
  - *Alternatives Considered*: Bundling a Python FastAPI backend inside the app, which would add massive binary bloat and execution overhead.
- **Decision 2: Local SQLite / Room Database Architecture**:
  - *Chosen Approach*: Clean Room database with relational entities: `ChatSession`, `ChatMessage`, `ProviderConfig`, and `PersonaPreset`.
  - *Why*: Provides reactive Kotlin `Flow` queries, transactional safety, fast search, zero latency, and works 100% offline.
  - *Alternatives Considered*: SharedPreferences or flat JSON files (lacks relational integrity and efficient pagination for long chats).
- **Decision 3: Unfiltered Freedom with Ethical Boundaries**:
  - *Chosen Approach*: System prompts emphasize objective, unbiased assistance for defensive security research, code analysis, and educational creativity without artificial moralizing or preachy refusals, while maintaining a clear disclaimer that upstream provider safety policies apply.

***

## 4. Technical Architecture & Data Strategy

### System Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        NEXORA AI (Android App)                         │
├────────────────────────────────────────────────────────────────────────┤
│  UI Layer (Jetpack Compose + Material 3)                               │
│  ┌───────────────────────┐  ┌─────────────────┐  ┌──────────────────┐  │
│  │ ChatScreen & Bubbles  │  │ Sidebar Drawer  │  │ Persona Selector │  │
│  │ (Markdown, Code Block)│  │ (Session List)  │  │ & System Studio  │  │
│  └───────────┬───────────┘  └────────┬────────┘  └────────┬─────────┘  │
│              │                       │                    │            │
│  Presentation Layer (ViewModels & StateFlow)                           │
│  ┌──────────────────────────────────────────────────────────────────┐  │
│  │ ChatViewModel (Streaming state, text buffer, active persona)     │  │
│  │ SettingsViewModel (Providers, API keys, test connection, stats)  │  │
│  └───────────────────────────────────┬──────────────────────────────┘  │
│                                      │                                 │
│  Domain & Data Repositories          │                                 │
│  ┌───────────────────────────────────┴──────────────────────────────┐  │
│  │ ChatRepository & ProviderRepository                              │  │
│  └─────────┬──────────────────────────────────────────┬─────────────┘  │
│            │                                          │                │
│  ┌─────────▼────────────────────────┐    ┌────────────▼──────────────┐ │
│  │ Network Client (OkHttp + SSE)    │    │ Local Database (Room DAO) │ │
│  │ - OpenAI / Groq / Ollama Client  │    │ - ChatSessionEntity       │ │
│  │ - Connection Tester Engine       │    │ - ChatMessageEntity       │ │
│  │ - Token & Latency Meter          │    │ - ProviderConfigEntity    │ │
│  │ - Streaming Chunk Parser         │    │ - PersonaPresetEntity     │ │
│  └─────────┬────────────────────────┘    └────────────┬──────────────┘ │
└────────────┼──────────────────────────────────────────┼────────────────┘
             │ (HTTPS / HTTP)                           │ (Encrypted/Private)
             ▼                                          ▼
┌───────────────────────────────┐          ┌─────────────────────────────┐
│  AI Providers (Cloud & Local) │          │  On-Device SQLite Storage   │
│  - OpenAI / OpenRouter / Groq │          │  - History, Backups, Keys   │
│  - DeepSeek / Custom Ollama   │          │  - Zero Cloud Telemetry     │
└───────────────────────────────┘          └─────────────────────────────┘
```

### Data Model & Entities
1. **`ProviderConfigEntity`**:
   - `id: String`, `name: String`, `baseUrl: String`, `apiKey: String`, `defaultModel: String`, `isActive: Boolean`, `isCustom: Boolean`.
2. **`PersonaPresetEntity`**:
   - `id: String`, `name: String`, `description: String`, `systemPrompt: String`, `temperature: Float`, `iconName: String`, `isDefault: Boolean`.
3. **`ChatSessionEntity`**:
   - `id: String`, `title: String`, `createdAt: Long`, `updatedAt: Long`, `providerId: String`, `modelId: String`, `personaId: String`, `isPinned: Boolean`.
4. **`ChatMessageEntity`**:
   - `id: String`, `sessionId: String`, `role: String` ("user" | "assistant" | "system"), `content: String`, `timestamp: Long`, `promptTokens: Int`, `completionTokens: Int`, `isStreaming: Boolean`.

### Interactive Component & State Mapping
- **`ChatTopBar`**: Displays session title, active model chip, persona switcher dropdown, and action menu (Export, Clear, Settings).
- **`MessageList`**: Renders dynamic user/assistant cards. Detects code fences (```` ```lang ... ``` ````) and renders distinct monospaced cards with syntax highlight style and copy button.
- **`ChatInputBar`**: Multi-line expandable text input, attachment/file text injector, stop streaming button (when generating), and send button.
- **`ProviderSettingsDialog`**: Input fields for Base URL, API Key, Model ID, plus an instant "Ping & Test" button returning latency (ms) or detailed error diagnostics.
