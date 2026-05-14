import { ReactNode } from 'react';

export function highlightKeyword(text: string | undefined, keyword: string): ReactNode {
  if (!text) {
    return null;
  }
  const normalizedKeyword = keyword.trim();
  if (!normalizedKeyword) {
    return text;
  }

  const lowerText = text.toLowerCase();
  const lowerKeyword = normalizedKeyword.toLowerCase();
  const nodes: ReactNode[] = [];
  let cursor = 0;
  let matchIndex = lowerText.indexOf(lowerKeyword);

  while (matchIndex >= 0) {
    if (matchIndex > cursor) {
      nodes.push(text.slice(cursor, matchIndex));
    }
    nodes.push(
      <mark className="search-highlight" key={`${matchIndex}-${normalizedKeyword}`}>
        {text.slice(matchIndex, matchIndex + normalizedKeyword.length)}
      </mark>,
    );
    cursor = matchIndex + normalizedKeyword.length;
    matchIndex = lowerText.indexOf(lowerKeyword, cursor);
  }

  if (cursor < text.length) {
    nodes.push(text.slice(cursor));
  }

  return nodes;
}
