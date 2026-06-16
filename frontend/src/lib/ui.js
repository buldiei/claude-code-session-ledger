import { writable } from 'svelte/store';

// Whether the keyboard-shortcuts help overlay is open. Pages read this to suppress
// their own hotkeys while the overlay is up.
export const helpOpen = writable(false);
