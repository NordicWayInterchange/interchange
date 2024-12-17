export const timeConverter = (time: number): string => {
    const date = new Date(time);
    const locale = navigator.language;

    const localeDateString = date.toLocaleDateString(locale, {
        day: "2-digit",
        month: "short",
        year: "numeric"
    });

    const localeTimeString = date.toLocaleTimeString(locale, {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
        hour12: false
    });

    return `${localeDateString}\u2003${localeTimeString}`;
};
