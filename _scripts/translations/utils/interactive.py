"""
Interactive prompts using rich library.

Provides beautiful CLI interactions with support for --yes flag.
"""

from typing import Optional, List, Dict, Callable
from rich.console import Console
from rich.prompt import Prompt, Confirm
from rich.progress import Progress, SpinnerColumn, TextColumn, BarColumn, TaskProgressColumn
from rich.table import Table
from rich.panel import Panel
from rich import box

console = Console()


def confirm_action(
    message: str,
    default: bool = True,
    auto_yes: bool = False
) -> bool:
    """
    Ask user for confirmation.

    Args:
        message: Message to display
        default: Default value if user just presses Enter
        auto_yes: If True, skip prompt and return default (for --yes flag)

    Returns:
        True if user confirmed, False otherwise

    Example:
        >>> confirmed = confirm_action("Delete this file?", default=False)
        >>> if confirmed:
        ...     print("Deleting...")
    """
    if auto_yes:
        console.print(f"[dim]{message} [auto-yes][/dim]")
        return default

    return Confirm.ask(message, default=default)


def prompt_text(
    message: str,
    default: Optional[str] = None,
    auto_yes: bool = False,
    auto_value: Optional[str] = None
) -> str:
    """
    Prompt user for text input.

    Args:
        message: Prompt message
        default: Default value
        auto_yes: If True, return auto_value or default without prompting
        auto_value: Value to return in auto mode

    Returns:
        User input or default/auto value

    Example:
        >>> category = prompt_text("Enter category:", default="general")
    """
    if auto_yes:
        result = auto_value or default or ""
        console.print(f"[dim]{message} [auto: {result}][/dim]")
        return result

    return Prompt.ask(message, default=default)


def prompt_choice(
    message: str,
    choices: List[str],
    default: Optional[str] = None,
    auto_yes: bool = False
) -> str:
    """
    Prompt user to choose from a list.

    Args:
        message: Prompt message
        choices: List of valid choices
        default: Default choice
        auto_yes: If True, return default without prompting

    Returns:
        Selected choice

    Example:
        >>> tone = prompt_choice(
        ...     "Select tone:",
        ...     ["friendly", "formal", "casual"],
        ...     default="friendly"
        ... )
    """
    if auto_yes and default:
        console.print(f"[dim]{message} [auto: {default}][/dim]")
        return default

    return Prompt.ask(message, choices=choices, default=default)


def show_change(
    change_type: str,
    key: str,
    old_text: Optional[str] = None,
    new_text: Optional[str] = None
):
    """
    Display a string change with color coding.

    Args:
        change_type: Type of change ('added', 'modified', 'removed')
        key: String key
        old_text: Old text (for modified/removed)
        new_text: New text (for added/modified)

    Example:
        >>> show_change('added', 'new_key', new_text='Hello World')
        >>> show_change('modified', 'changed_key', old_text='Old', new_text='New')
    """
    if change_type == 'added':
        console.print(f"[green]+[/green] Added: [bold]{key}[/bold]")
        if new_text:
            console.print(f"  Text: [green]{new_text}[/green]")

    elif change_type == 'modified':
        console.print(f"[yellow]~[/yellow] Modified: [bold]{key}[/bold]")
        if old_text and new_text:
            console.print(f"  Old: [red]{old_text}[/red]")
            console.print(f"  New: [green]{new_text}[/green]")

    elif change_type == 'removed':
        console.print(f"[red]-[/red] Removed: [bold]{key}[/bold]")
        if old_text:
            console.print(f"  Last text: [dim]{old_text}[/dim]")


def show_progress(
    total: int,
    description: str = "Processing"
) -> Progress:
    """
    Create and return a progress bar.

    Args:
        total: Total number of items
        description: Description text

    Returns:
        Progress object (use as context manager)

    Example:
        >>> with show_progress(100, "Translating") as progress:
        ...     task = progress.add_task("", total=100)
        ...     for i in range(100):
        ...         progress.update(task, advance=1)
    """
    return Progress(
        SpinnerColumn(),
        TextColumn("[progress.description]{task.description}"),
        BarColumn(),
        TaskProgressColumn(),
        console=console
    )


def show_summary_table(
    title: str,
    data: List[Dict[str, str]],
    headers: Optional[List[str]] = None
):
    """
    Display a summary table.

    Args:
        title: Table title
        data: List of dictionaries with row data
        headers: List of column headers (uses first dict keys if None)

    Example:
        >>> data = [
        ...     {'Flavor': 'shared', 'Strings': '144', 'Status': 'Ready'},
        ...     {'Flavor': 'app-free', 'Strings': '50', 'Status': 'Ready'}
        ... ]
        >>> show_summary_table("Flavors", data)
    """
    if not data:
        console.print(f"[dim]No data to display for {title}[/dim]")
        return

    if headers is None:
        headers = list(data[0].keys())

    table = Table(title=title, box=box.ROUNDED)

    for header in headers:
        table.add_column(header, style="cyan")

    for row in data:
        table.add_row(*[str(row.get(h, "")) for h in headers])

    console.print(table)


def show_panel(
    content: str,
    title: Optional[str] = None,
    style: str = "blue"
):
    """
    Display content in a panel.

    Args:
        content: Content to display
        title: Optional panel title
        style: Panel border style/color

    Example:
        >>> show_panel("Migration complete!", title="Success", style="green")
    """
    console.print(Panel(content, title=title, border_style=style))


def show_error(message: str):
    """
    Display an error message.

    Args:
        message: Error message

    Example:
        >>> show_error("Failed to load configuration")
    """
    console.print(f"[bold red]Error:[/bold red] {message}")


def show_warning(message: str):
    """
    Display a warning message.

    Args:
        message: Warning message

    Example:
        >>> show_warning("Some translations are missing")
    """
    console.print(f"[bold yellow]Warning:[/bold yellow] {message}")


def show_success(message: str):
    """
    Display a success message.

    Args:
        message: Success message

    Example:
        >>> show_success("All strings translated successfully")
    """
    console.print(f"[bold green]Success:[/bold green] {message}")


def show_info(message: str):
    """
    Display an info message.

    Args:
        message: Info message

    Example:
        >>> show_info("Processing 100 strings...")
    """
    console.print(f"[bold blue]Info:[/bold blue] {message}")


def show_header(text: str):
    """
    Display a header.

    Args:
        text: Header text

    Example:
        >>> show_header("Synchronizing Translations")
    """
    console.print()
    console.rule(f"[bold cyan]{text}[/bold cyan]")
    console.print()


def show_separator():
    """Display a separator line."""
    console.print("[dim]" + "-" * console.width + "[/dim]")
