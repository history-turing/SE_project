type IconName =
  | 'home'
  | 'topics'
  | 'alumni'
  | 'profile'
  | 'search'
  | 'bell'
  | 'heart'
  | 'bookmark'
  | 'chat'
  | 'send'
  | 'plus'
  | 'spark'
  | 'close';

interface IconProps {
  name: IconName;
  className?: string;
}

export function Icon({ name, className }: IconProps) {
  const props = {
    className,
    fill: 'none',
    stroke: 'currentColor',
    strokeLinecap: 'round' as const,
    strokeLinejoin: 'round' as const,
    strokeWidth: 1.8,
    viewBox: '0 0 24 24',
  };

  switch (name) {
    case 'home':
      return (
        <svg {...props}>
          <path d="M3 10.8 12 4l9 6.8" />
          <path d="M5.5 10.5V20h13V10.5" />
          <path d="M10 20v-5h4v5" />
        </svg>
      );
    case 'topics':
      return (
        <svg {...props}>
          <path d="M5 7.5h14" />
          <path d="M5 12h14" />
          <path d="M5 16.5h10" />
          <path d="M16 15.5 19 18.5 21 16.5" />
        </svg>
      );
    case 'alumni':
      return (
        <svg {...props}>
          <path d="M8 11a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z" />
          <path d="M16.5 10a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Z" />
          <path d="M3.5 19c.8-2.7 3.1-4 6.2-4 3.2 0 5.3 1.4 6.1 4" />
          <path d="M14.5 18.8c.4-1.9 1.9-3 4.2-3 1 0 1.9.2 2.8.7" />
        </svg>
      );
    case 'profile':
      return (
        <svg {...props}>
          <path d="M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" />
          <path d="M4.5 20c1.2-3.1 3.8-4.7 7.5-4.7 3.7 0 6.3 1.6 7.5 4.7" />
        </svg>
      );
    case 'search':
      return (
        <svg {...props}>
          <circle cx="11" cy="11" r="6.5" />
          <path d="m16 16 4 4" />
        </svg>
      );
    case 'bell':
      return (
        <svg {...props}>
          <path d="M12 20a2.5 2.5 0 0 0 2.4-1.8" />
          <path d="M18 15.5H6c1.2-1.2 1.8-2.9 1.8-4.8V9.9a4.2 4.2 0 1 1 8.4 0v.8c0 1.9.6 3.6 1.8 4.8Z" />
        </svg>
      );
    case 'heart':
      return (
        <svg {...props}>
          <path d="M12 20s-7-4.6-7-10.3C5 6.7 7 5 9.4 5c1.4 0 2.2.6 2.6 1.4.4-.8 1.2-1.4 2.6-1.4C17 5 19 6.7 19 9.7 19 15.4 12 20 12 20Z" />
        </svg>
      );
    case 'bookmark':
      return (
        <svg {...props}>
          <path d="M7 4.5h10V20l-5-3.3L7 20V4.5Z" />
        </svg>
      );
    case 'chat':
      return (
        <svg {...props}>
          <path d="M5 6.5h14v9H9l-4 3v-12Z" />
        </svg>
      );
    case 'send':
      return (
        <svg {...props}>
          <path d="m3 11.5 17-7-5.8 15-2.4-5.8L3 11.5Z" />
          <path d="M20 4.5 11.8 13.7" />
        </svg>
      );
    case 'plus':
      return (
        <svg {...props}>
          <path d="M12 5v14" />
          <path d="M5 12h14" />
        </svg>
      );
    case 'spark':
      return (
        <svg {...props}>
          <path d="m12 3 1.9 5.1L19 10l-5.1 1.9L12 17l-1.9-5.1L5 10l5.1-1.9L12 3Z" />
        </svg>
      );
    case 'close':
      return (
        <svg {...props}>
          <path d="m6 6 12 12" />
          <path d="M18 6 6 18" />
        </svg>
      );
  }
}
