# Per-ROM save storage and migration

This build stores every ROM's save data in a deterministic, separate folder.

## New layout

```text
<data-directory>/
├── saves/
│   └── <rom-name>__<identity-hash>/
│       └── save.srm
├── states/
│   └── <core-name>/
│       └── <rom-name>__<identity-hash>/
│           ├── autosave.state
│           ├── autosave.state.metadata
│           ├── slot1.state
│           ├── slot1.state.metadata
│           └── ...
└── state-previews/
    └── <core-name>/
        └── <rom-name>__<identity-hash>/
            ├── slot1.jpg
            └── ...
```

The folder name uses a sanitized ROM basename plus a 16-character SHA-256 identity derived from the ROM URI, system, and (for states/previews) core. This prevents different ROMs with the same filename from sharing the same new folder.

## Migration

Migration is lazy and idempotent. When a game is first read or written:

1. The manager checks the new per-ROM folder.
2. If a new file is missing, it looks for the old flat filename and the previously supported hashed filename.
3. The newest non-empty legacy candidate is copied into the new folder.
4. The legacy file is retained, so the operation is non-destructive and safe to retry.
5. Future reads and writes use only the new per-ROM folder.

Save RAM migrates from `saves/<basename>.srm` and the old hashed `.srm` name. Save states migrate from `states/<core>/<basename>.state`, `.slotN`, metadata files, and their old hashed equivalents. State previews migrate from `state-previews/<core>/<basename>.slotN.jpg` and the old hashed equivalents.

If multiple old ROMs had the exact same basename, the old flat layout did not contain enough information to distinguish them. Those ambiguous legacy files can only be copied to the first matching game encountered; new saves made after this build are fully separated by identity folder.
