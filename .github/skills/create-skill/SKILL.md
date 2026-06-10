---
name: create-skill
description: "Create a reusable skill from a workflow observed in conversation. Use when a repeatable multi-step process should be packaged into a SKILL.md file."
argument-hint: "What workflow should this skill capture?"
disable-model-invocation: true
---

# Create a Skill

## When to Use
- The user is following a repeatable, multi-step workflow.
- The workflow should be reusable in future chats or across the workspace.
- The user wants a new SKILL.md for a task such as debugging, review, setup, implementation, or documentation.

## Procedure
1. Review the conversation and identify the workflow.
   - Extract the step-by-step process being followed.
   - Note decision points and branching logic.
   - Capture quality criteria or completion checks.
2. Clarify requirements if needed.
   - Ask what outcome the skill should produce.
   - Confirm whether it should be workspace-scoped or personal.
   - Decide whether it should be a quick checklist or a full multi-step workflow.
3. Draft the skill.
   - Create or update a SKILL.md file at .github/skills/<skill-name>/SKILL.md for workspace-scoped use.
   - Use a clear name that matches the folder name.
   - Write a description that makes the skill easy to discover.
4. Refine the skill.
   - Make the procedure concrete and actionable.
   - Keep the instructions focused and practical.
   - Add references only when they improve the workflow.
5. Finalize and explain it.
   - Summarize what the skill produces.
   - Suggest example prompts that would invoke the skill.
   - Propose related customizations to create next.

## Quality Bar
- The skill must be reusable rather than a one-off note.
- The instructions should be specific and actionable.
- The description should clearly indicate when the skill should be used.
- The workflow should include explicit steps and clear completion criteria.
