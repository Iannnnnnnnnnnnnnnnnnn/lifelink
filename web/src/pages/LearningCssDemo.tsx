import '../styles/learning-css-demo.css';

const cardItems = [
  { title: '卡片 A', description: '固定最小宽度，空间不足时自动换行。' },
  { title: '卡片 B', description: '适合商品、文章、功能入口等列表。' },
  { title: '卡片 C', description: '每张卡片保持同一套弹性布局规则。' },
  { title: '卡片 D', description: '容器变窄时，卡片会换到下一行。' },
  { title: '卡片 E', description: '容器变宽时，一行会展示更多卡片。' },
  { title: '卡片 F', description: '不需要手动计算每行数量。' },
];

export function LearningCssDemo() {
  return (
    <div className="page-wide learning-css-demo-page">
      <div className="page-heading learning-css-demo-heading">
        <div>
          <h2>CSS Flex 布局练习</h2>
          <span className="learning-css-demo-subtitle">通过三个常见场景观察 flex 的排列、对齐和换行效果。</span>
        </div>
      </div>

      <section className="learning-css-demo-section">
        <div className="learning-css-demo-copy">
          <h3>水平居中布局</h3>
          <p>用途：让按钮、提示块、空状态等单个元素在容器水平方向居中展示。</p>
        </div>
        <div className="learning-css-demo-center-box">
          <div className="learning-css-demo-center-item">水平居中的内容</div>
        </div>
      </section>

      <section className="learning-css-demo-section">
        <div className="learning-css-demo-copy">
          <h3>左右两栏布局</h3>
          <p>用途：左侧放导航、筛选或说明，右侧放主要内容，常见于后台列表和详情页面。</p>
        </div>
        <div className="learning-css-demo-two-column">
          <aside className="learning-css-demo-side-panel">
            <span>左侧栏</span>
            <p>适合放筛选条件、目录或辅助信息。</p>
          </aside>
          <div className="learning-css-demo-main-panel">
            <span>右侧内容区</span>
            <p>主内容区域会占据剩余空间，并随窗口宽度自适应。</p>
          </div>
        </div>
      </section>

      <section className="learning-css-demo-section">
        <div className="learning-css-demo-copy">
          <h3>卡片列表自适应换行布局</h3>
          <p>用途：展示功能入口、商品列表、文章列表等数量不固定的卡片内容。</p>
        </div>
        <div className="learning-css-demo-card-list">
          {cardItems.map((item) => (
            <article key={item.title} className="learning-css-demo-card">
              <h4>{item.title}</h4>
              <p>{item.description}</p>
            </article>
          ))}
        </div>
      </section>
    </div>
  );
}
