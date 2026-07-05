# Domain Model Design Guide for AI (Claude / Cursor)

Use this file as the **single source of instructions** when helping design and draw a **domain model**.

The goal is to help the AI stay in the **problem space**, not drift into software design too early.

---

## 1. What a domain model is

A **domain model** captures the **entities or concepts in the domain** that are relevant to the problem, and the **relationships between them**.

It is a tool for understanding the **problem space**:
- what concepts exist
- how they relate
- what abstractions are important

A domain model is used to **characterise the domain**, not to design the final software solution.

---

## 2. What a domain model is NOT

The AI must strictly follow these rules:

### Not a UML class diagram
A domain model is **not** a class diagram.

Do **not** treat it like an object-oriented design artifact.

### No attributes
Do **not** include:
- fields
- data members
- attributes
- properties inside the boxes

Each concept should appear only as a **named entity/concept**, usually in a simple rectangle.

### No behaviour
Do **not** include:
- methods
- operations
- responsibilities
- functions attached to entities

### Not solution space
Do **not** jump into:
- implementation
- database schema
- tables
- APIs
- software architecture
- code structure
- classes for implementation

The domain model must remain in the **problem space**.

### Not an ER diagram
Even though there may be similarities in notation or cardinality, the goal is **not** to design a database.

---

## 3. Core mindset the AI must follow

When helping build the model, the AI must think like this:

- Focus on **what exists in the domain**
- Focus on **how concepts relate**
- Avoid discussing **how the system will implement it**
- Avoid design-level detail unless explicitly asked later
- Keep the model abstract and concept-focused

---

## 4. How to identify domain entities

### If a problem description or transcript exists
Extract likely entities by scanning for **nouns**.

Nouns often indicate candidate domain entities.

But the AI must not blindly include every noun, because:
- some nouns are not true domain entities
- multiple nouns may refer to the same concept
- some concepts may be implied and not stated directly

So the process should be:

1. Read the description/transcript carefully
2. Extract likely nouns/concepts
3. Merge synonyms or duplicates
4. Remove irrelevant nouns
5. Keep only the concepts that matter to understanding the domain

### If there is no detailed textual description
Use **domain knowledge** to infer relevant concepts.

This is important because sometimes the case does not list every concept explicitly. A good domain model may require introducing additional concepts that make the structure correct and clearer.

---

## 5. How to identify relationships

After identifying the entities, revisit the description and extract:
- associations
- structural relationships
- part-whole relationships
- generalisations / specialisations

The AI should always ask:

- How are these two concepts related?
- Does one concept contain another?
- Is one concept a special kind of another?
- Does one concept depend on or use another?
- Is a missing abstraction needed to make the model correct?

---

## 6. Allowed relationship types

The AI may use the following relationships when appropriate.

### 6.1 Association
Use for general relationships between concepts.

Example:
- Customer **makes** Transaction
- Doctor **treats** Patient

Helpful rules:
- relationship names should be meaningful
- direction can help readability
- cardinality may be added if useful

### 6.2 Aggregation
Use when one concept is made up of or has other parts, but the parts can still conceptually exist independently.

Example:
- Car has Wheels

Aggregation is often the safer/simple choice if unsure between aggregation and composition.

### 6.3 Composition
Use when one concept’s parts only exist within the context of the whole.

Example from transcript:
- Class is composed of Methods
- Class is composed of Fields

Reason:
Methods and fields do not meaningfully exist outside the context of a class in that example.

### 6.4 Generalisation
Use when one concept is a more specific type of another.

Examples:
- Payment Transaction is a kind of Transaction
- Refund Transaction is a kind of Transaction
- Car is a kind of Vehicle
- Bus is a kind of Vehicle

Use this often when the domain clearly has categories and subtypes.

---

## 7. Cardinality guidance

Cardinality can be included where it adds clarity.

Examples:
- one customer can make many transactions
- one transaction belongs to one customer

Use cardinalities only when they are meaningful and grounded in the problem/domain.

Do not invent very specific multiplicities unless they are reasonably justified.

---

## 8. Important modelling rules from the transcript

These are the most important instructions the AI must obey:

### Rule 1: Stay abstract
Only include **concepts/entities** and **relationships**.

### Rule 2: No attributes, no behaviour
Even if many tools or AI systems tend to add them, **do not add them** for this task.

### Rule 3: Iteration is expected
The first version will probably be incomplete or slightly wrong.

The AI should help with domain modelling as an **iterative refinement process**.

### Rule 4: Domain knowledge matters
A correct model may require introducing concepts that were not explicitly listed but are needed to make the model coherent.

### Rule 5: Add missing abstractions when needed
If relationships become awkward or inconsistent, the AI should consider whether a missing higher-level concept is needed.

Example from transcript:
- introducing **Type**
- introducing **Signature**

These extra abstractions made the model more correct and expressive.

### Rule 6: Prefer correctness over being simplistic
A too-simple domain model may miss important concepts and become misleading.

---

## 9. Iterative workflow the AI should follow

When helping draw the domain model, the AI should follow this workflow:

### Step 1: Extract candidate entities
List the main concepts in the domain.

### Step 2: Remove weak or duplicate concepts
Merge synonyms and remove concepts that are not central.

### Step 3: Add initial relationships
Connect the concepts with:
- association
- aggregation/composition
- generalisation

### Step 4: Review for gaps
Ask:
- Is any key concept missing?
- Are any relationships awkward?
- Do we need a more abstract parent concept?
- Do we need a bridging concept?

### Step 5: Refine
Revise the entity set and the relationships.

### Step 6: Produce the final domain model
Keep it:
- conceptually correct
- concise
- abstract
- free from attributes/behaviour

---

## 10. Common AI mistakes to avoid

The AI must avoid these mistakes:

- turning the model into a UML class diagram
- adding attributes inside entities
- adding methods or operations
- introducing implementation classes
- drifting into database tables
- designing APIs or endpoints
- assuming the first attempt is correct
- failing to introduce missing concepts when needed
- overcomplicating with technical/software solution details

---

## 11. What “good” AI assistance looks like

When assisting with the domain model, the AI should:

- identify the main domain concepts
- explain why each concept is included
- explain why each relationship is used
- suggest missing abstractions if needed
- refine the model over multiple passes
- justify composition vs aggregation vs association vs generalisation
- remain in the problem space throughout

---

## 12. Output format the AI should ideally produce

When asked to help, the AI should provide:

### A. Candidate entities
A clean list of domain concepts.

### B. Relationships
A structured list such as:
- `Entity A --[relationship]--> Entity B`
- include cardinality where useful
- include composition / aggregation / generalisation where appropriate

### C. Missing concept suggestions
If the first pass seems incomplete, explicitly propose extra abstractions.

### D. Final drawing guidance
A simplified textual layout the user can turn into a diagram.

Example format:
- `Customer --makes--> Transaction`
- `Transaction --uses--> Payment Token`
- `Payment Transaction --is a--> Transaction`
- `Refund Transaction --is a--> Transaction`

---

## 13. Recommended prompting style for Claude / Cursor

Use the following prompt when asking the AI to help with the actual domain model:

---

### Prompt to give the AI

You are helping me create a **domain model**, not a UML class diagram and not a database ERD.

Follow these strict rules:
1. Stay in the **problem space**, not the solution space.
2. Identify only **domain entities/concepts** and **relationships**.
3. **Do not include attributes, fields, properties, methods, or behaviour.**
4. Use only high-level relationships such as:
   - association
   - aggregation
   - composition
   - generalisation
5. Cardinalities may be added only when useful and justified.
6. Treat domain modelling as an **iterative refinement process**.
7. If the model seems incomplete, suggest **missing abstractions/concepts** that improve correctness.
8. Explain why each entity and relationship belongs in the domain model.
9. Do not drift into implementation, database design, APIs, software classes, or architecture.

Please do the following:
1. Extract the candidate domain entities from the case description I provide.
2. Remove duplicates or weak concepts.
3. Propose the main relationships between the entities.
4. Identify any missing higher-level abstractions or bridging concepts.
5. Refine the model into a clean final domain model.
6. Present the result as:
   - a list of entities
   - a list of relationships
   - a short explanation of modelling decisions
   - a text representation I can directly draw

---

## 14. Final reminder for the AI

A domain model should answer:

- What are the important concepts in this domain?
- How are they related?
- What abstractions help us understand the domain correctly?

It should **not** answer:

- What classes should I code?
- What attributes should each object have?
- What database tables should exist?
- What methods should be implemented?

Keep the result **clean, abstract, and conceptually correct**.

---