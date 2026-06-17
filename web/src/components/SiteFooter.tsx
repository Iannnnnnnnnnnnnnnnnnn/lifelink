const POLICE_BEIAN_URL = 'https://beian.mps.gov.cn/#/query/webSearch?code=22040002000169';

interface SiteFooterProps {
  className?: string;
}

export function SiteFooter({ className }: SiteFooterProps) {
  const footerClassName = className ? `site-footer ${className}` : 'site-footer';

  return (
    <div className={footerClassName}>
      <div className="site-footer-content">
        <span className="site-footer-copyright">© 2026 LifeLink</span>
        <a
          href={POLICE_BEIAN_URL}
          rel="noreferrer"
          target="_blank"
          className="police-beian-link"
        >
          <img
            src="/beian-police.png"
            alt="公安联网备案图标"
            className="police-beian-icon"
          />
          <span>吉公网安备22040002000169号</span>
        </a>
      </div>
    </div>
  );
}
