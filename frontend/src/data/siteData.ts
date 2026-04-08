import type {
  AlumniContact,
  Conversation,
  FeedPost,
  NavItem,
  NoticeItem,
  RankingItem,
  StoryCard,
  TopicGroup,
  UserProfile,
} from '../types';

export const navItems: NavItem[] = [
  { path: '/', label: '首页', icon: 'home' },
  { path: '/topics', label: '话题', icon: 'topics' },
  { path: '/alumni', label: '校友圈', icon: 'alumni' },
  { path: '/profile', label: '我的', icon: 'profile' },
];

export const publishTopics = [
  '校园日常',
  '学业交流',
  '表白墙',
  '失物招领',
  '职场内推',
  '校友故事',
  '生活闲聊',
];

export const topicGroups: TopicGroup[] = [
  {
    id: 'confession',
    name: '表白墙',
    description: '把没说出口的话，放进珞珈山的风里。',
    heat: '1.2k 正在热议',
    destination: '/',
    accent: 'rose',
    tags: ['心动瞬间', '暗恋日记', '春日樱花'],
    emoji: '💗',
  },
  {
    id: 'lost-found',
    name: '失物招领',
    description: '连接遗落的时光，帮物品重新找到主人。',
    heat: '450+ 待认领',
    destination: '/',
    accent: 'jade',
    tags: ['校园卡', '钥匙', '雨伞'],
    emoji: '🔎',
  },
  {
    id: 'study',
    name: '学业交流',
    description: '课程、考研、复习、书单与经验都在这里汇流。',
    heat: '学术研讨中',
    destination: '/',
    accent: 'gold',
    tags: ['期末周', '考研经验', '课程互助'],
    emoji: '📚',
  },
  {
    id: 'campus-chat',
    name: '校园闲谈',
    description: '吐槽食堂、分享晚霞、记录普通却动人的一天。',
    heat: '深夜食堂',
    destination: '/',
    accent: 'ink',
    tags: ['食堂测评', '东湖日落', '宿舍闲聊'],
    emoji: '☕',
  },
  {
    id: 'career',
    name: '职场内推',
    description: '把校友网络织进求职旅程，给下一位武大人一束光。',
    heat: '158 条机会',
    destination: '/alumni',
    accent: 'jade',
    tags: ['产品经理', '算法岗', '春招'],
    emoji: '💼',
  },
  {
    id: 'alumni-stories',
    name: '校友故事',
    description: '看见从珞珈到世界各地的人，如何讲述自己的路。',
    heat: '持续更新',
    destination: '/alumni',
    accent: 'rose',
    tags: ['行业成长', '返校记忆', '人生选择'],
    emoji: '🌸',
  },
];

export const topicRankings: RankingItem[] = [
  { id: '1', label: '#樱花季预约', heat: '45.2w 热度' },
  { id: '2', label: '#图书馆占座', heat: '32.8w 热度' },
  { id: '3', label: '#梅园食堂新品', heat: '28.5w 热度' },
  { id: '4', label: '#珞珈山猫咪图鉴', heat: '15.1w 热度' },
  { id: '5', label: '#春招提前批', heat: '12.3w 热度' },
];

export const campusNotices: NoticeItem[] = [
  { id: '1', title: '樱花开放期间校园管理措施更新', meta: '置顶公告' },
  { id: '2', title: '图书馆预约系统今晚 23:00 维护', meta: '系统通知' },
  { id: '3', title: '东湖夜跑社团本周五集合', meta: '社团活动' },
];

export const initialCommunityPosts: FeedPost[] = [
  {
    id: 'home-1',
    title: '今早的老斋舍，樱花落了一地',
    content:
      '时间好像在这里走得很慢。看到花瓣落在台阶上，突然觉得三年前拖着箱子第一次进校门的那个人还在眼前。',
    author: '小狐狸树洞',
    handle: '匿名珞珈人',
    topic: '校园日常',
    audience: '首页',
    createdAt: '2 小时前',
    likes: 421,
    comments: 56,
    saves: 18,
    accent: 'rose',
    image:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuD8CSpYSgaCQvtSochwfAUj5xcEaCndrkiI7ZKsODwr7Ys8dmr4SGagami3nLfaafgTcvqi054ztrBbj4SJbO2vT7aJ7oNNHJezM2CzZ0_176jy7l8_D_VFOI9Ib3vuXZU2oVrvo9rDZI8ggRsCAaSE73Y7-PvwKIqdPuL5PRP71MFy2-oM1BA9bA30IwJ_DKkr0ZKOUplN9CLXVvkIKlPwZbYFk2D1tzj9ZHkKTQg6edgH8K3Hh2mIcIymwARKWY7y7Q_Pqp0HYEY',
    anonymous: true,
  },
  {
    id: 'home-2',
    title: '有没有跨专业考研到法学院的前辈？',
    content:
      '本科是理科生，一直想转向法学方向，最近开始认真查资料了。求经验、求书单，哪怕一点建议也很珍贵。',
    author: '匿名用户',
    handle: '学业互助',
    topic: '学业交流',
    audience: '首页',
    createdAt: '今天 11:20',
    likes: 89,
    comments: 24,
    saves: 37,
    accent: 'gold',
    anonymous: true,
  },
  {
    id: 'home-3',
    content: '东湖的晚霞真的很适合给焦虑按下暂停键。今晚风很大，心却很静。',
    author: '追夕阳的人',
    handle: '湖边散步计划',
    topic: '校园日常',
    audience: '首页',
    createdAt: '昨天',
    likes: 842,
    comments: 45,
    saves: 61,
    accent: 'ink',
    image:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuDHKR0IUL049QAM0PLzVEKHLp7HOzal0p3ANiMWS2K9y5f_u05R32GLxwjhv7KFxSLIy9d0y6vUznx3_8hB9m0njdkQPQ-Zm1JrfqjON9_LAYpo_ftuWwdW7F7oyD_hNC9GxKX09kWSppz2KvVGTzfeW5OQLRnlbNn9Djf3rfzd_79tAA56fC3EezYjwil7ZwDtcrxUd8150oBbToKnrjAgbkY3xEWTq-A-nNRy99OHA5UCTmYuECozDqEHtN9vCCn6C7O-iqD-GEU',
  },
  {
    id: 'home-4',
    title: '终于在工学部食堂吃到了今天最满意的一碗热干面',
    content:
      '如果有人也在做“武大食堂巡礼”，真心推荐这家。辣油香得刚刚好，面也够劲道。',
    author: '干饭人小张',
    handle: '食堂雷达',
    topic: '生活闲聊',
    audience: '首页',
    createdAt: '昨天',
    likes: 3200,
    comments: 128,
    saves: 205,
    accent: 'jade',
    image:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuBg1h0UmfI-Dk8CBqaiQ5o1d3EEPsLx-qMUXdV2eFEeTfadg1SPoZhWCL4QeykemOhZLcSOLtXBCasrWVEt_uxU8bztEBLT4wZWQh2CHXqQpssn0VqX3Mn60rnQFfXffXc3KDfHv9vgorCNaG-D24jsvK2uT26kf7nxRpyhgC8KhZ96zgIUgkl1fhtx0zsgYNgHwEVi548eu00EmlLp1SKZddbQ99Gka-Q6bxB59OMWBR1Su_kef48Ks1eAFY5vuLXJWZKOOEE_usQ',
  },
];

export const initialAlumniPosts: FeedPost[] = [
  {
    id: 'alumni-1',
    title: '回母校走走，樱花大道依旧，只是少年已不再',
    content:
      '今天趁着出差回了趟武大，在梅园吃了一碗热干面，味道还是那个味。看到图书馆里埋头苦读的学弟学妹，仿佛看到了当年的自己。',
    author: '陈先生',
    handle: '2015 级校友',
    topic: '校友故事',
    audience: '校友圈',
    createdAt: '深圳 · 2 小时前',
    likes: 1200,
    comments: 86,
    saves: 53,
    accent: 'rose',
    badge: '返校日记',
    image:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuCNiRhjspAGQusQ9IEMiBqpuWXYlSEpvExZ0Uu5ytSh4ijd-u0YTRpSGh996a1DBJLUlJJs-5qHMA155OHQ3xnLlSpEXxAGlq8sLthN5YTwoIfUJvfE9Cjob3aUiKRAYxfXaCd2gS6xlFxk-LEKlKI_E98YpswyawdIw5EO7haBOI_tKQYjCa6KJDOay3kuTPh_QXcBf7n5pkx0zJFJm9Kwu8FkToFJpe0mRbSSIdZkgnoKu_ovwMVGWx2ApaeP-2BDuEbg22rnXkE',
    location: '深圳',
  },
  {
    id: 'alumni-2',
    title: '【字节跳动】产品经理 / 研发校招社招内推',
    content:
      '部门直招，主要负责电商相关业务。校友内推简历直达 HR，通过率更高。感兴趣的同学或校友可以直接私信我。',
    author: '林学姐',
    handle: '2018 级校友',
    topic: '职场内推',
    audience: '校友圈',
    createdAt: '上海 · 5 小时前',
    likes: 452,
    comments: 120,
    saves: 141,
    accent: 'jade',
    badge: '机会速递',
    location: '上海',
  },
  {
    id: 'alumni-3',
    title: '支教归来的这一年，重新理解了“成长”',
    content:
      '离开校园后才发现，很多答案不是在课堂里得到的，而是在与真实世界的相遇里慢慢长出来的。很想和学弟学妹聊聊“理想主义”的落地方法。',
    author: '李校友',
    handle: '2018 级 · 文学院',
    topic: '生活闲聊',
    audience: '校友圈',
    createdAt: '成都 · 昨天',
    likes: 284,
    comments: 41,
    saves: 29,
    accent: 'gold',
    badge: '成长故事',
    location: '成都',
  },
];

export const initialMyPosts: FeedPost[] = [
  {
    id: 'me-1',
    title: '想给第一次来武大的朋友做一份散步地图',
    content:
      '从凌波门日出、老图书馆、万林艺术博物馆到傍晚的东湖绿道，如果你只能在武大待一天，我很想把这条线送给你。',
    author: '樱花味猫奴',
    handle: '我的树洞',
    topic: '校园日常',
    audience: '首页',
    createdAt: '3 天前',
    likes: 96,
    comments: 14,
    saves: 22,
    accent: 'rose',
  },
  {
    id: 'me-2',
    title: '如果你也在准备春招，我整理了一份时间线',
    content:
      '把最近看到的笔试、投递和面试时间都汇总到了便签里。不是最全，但希望能帮到正在赶路的人。',
    author: '樱花味猫奴',
    handle: '我的树洞',
    topic: '职场内推',
    audience: '校友圈',
    createdAt: '1 周前',
    likes: 143,
    comments: 27,
    saves: 49,
    accent: 'jade',
  },
];

export const alumniStories: StoryCard[] = [
  { id: 'story-1', title: '从珞珈山到硅谷', meta: '2012 级 计算机学院 · 张校友' },
  { id: 'story-2', title: '支教归来的这一年', meta: '2018 级 文学院 · 李校友' },
  { id: 'story-3', title: '转行做纪录片导演之后', meta: '2014 级 新闻学院 · 吴校友' },
];

export const alumniContacts: AlumniContact[] = [
  {
    id: 'wang',
    name: '王博士',
    meta: '2010 级 · 物理学院',
    focus: '材料与科研合作',
    avatar:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuCFmfkwIglgxkmaM9yKGs8HUdPQsbm1J-b0I8QuDow7nj7yHWefvxoyFQeYTkyY6BKEdCaUmj_imnabW9QJQrgxoj06yPh9UwFQR0hEw9HVzBNw348TuSujriQp5m2MM1-egTwTGa3toFS2NV1FRmohnNI0eHb2yHw2XpIwygJ7rTC72_yJwwKOzS4flC39f52-eh-uatb2AiBquGQuuRi8BZeLxegO35e9SWEHGjwJPmoKpHUfAM3FERCCUVaDM6vHFs9lBWdmizY',
  },
  {
    id: 'zhao',
    name: '赵设计师',
    meta: '2019 级 · 艺术学院',
    focus: '品牌与视觉设计',
    avatar:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuA8HD3dWJU7oBrSRwI_ar_moMHUCkUCmrC9pv4FX48LCesknV47gcvR8-LNfgJYijwnj5c7wZMAGETf7cIjyXnzfWp3c-8Wq3E3veIJ3YZpnnK9dhhHQTTO0enu9lPRuFtGng2Y5J-qc8105ynTHV3xzLmG_U7n5M9tpfajsjzmaWXuy5NN_S-nastnOnlSsk1_wxU18I9ni33yomvlzHpWgFyphzVb_whiHzcV2KlR6u5rjc97xZTkchU6yHtBlMkuMlRb6NSAkhs',
  },
  {
    id: 'zhou',
    name: '周律师',
    meta: '2008 级 · 法学院',
    focus: '法律咨询与职业路径',
    avatar:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuD-C6x6pekP-X5Hfp5wAaW66n3yp-Lc7shg9zWu-AIBsUrME_eX3hvCZYLZNTdN_ZgrDztuF6vo82ewrmYYOaL7QEzwTFjFNaKYMOcYWmshKCPQ7NktcXQogZx--DlPc9b9E0XeGBsMhgiUheCngoiBFElFywybwNNhWO0JHvEeLxewAVJvyZ0-ayX0LIGquqNAp4xgTa0BoVot7njrXQrlZygoydB5RI2NqKXPc5Btb00c596e2YWYgEL0da9GvgaF7zymB0HNk80',
  },
];

export const profile: UserProfile = {
  name: '樱花味猫奴',
  tagline: '把校园生活慢慢写成一册柔软的日志。',
  college: '信息管理学院',
  year: '2022 级',
  bio:
    '喜欢晚霞、热干面、图书馆靠窗位置，还有把别人觉得平凡的片刻认真记下来。当前版本中的个人主页兼容了原消息中心页面。',
  avatar:
    'https://lh3.googleusercontent.com/aida-public/AB6AXuCRFlbLzIinj2Jr_e5FZaU8PzhsGIJubcOacGoZU9rIXIn-j31qn26JuNNPALl5X5UUIzDXoIBGIlvwuLJLyeXAY1SQh0oof205Id-TssH6yLOn8zbVCF-A5bFRsOX4FcKfEBM6qe9xebwoNqRj5W5xwEWmg9auY-VMlluHXh2vZRki7GxvAJATbVf_x-8flJUSrq-_zbG85Gtr-sk9hae4g5ecKp5SZSmQPn8JNvQ7cevTOTjXV7SS_MW_1PV5mcZ6iQOuVAWnBcI',
  badges: ['树洞记录者', '春招互助', '东湖散步搭子'],
  stats: [
    { label: '已发树洞', value: '18' },
    { label: '收藏内容', value: '27' },
    { label: '已建立私信', value: '9' },
  ],
};

export const initialConversations: Conversation[] = [
  {
    id: 'fox',
    name: '珞珈山下的小狐狸',
    subtitle: '学业互助伙伴',
    avatar:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuDPQaV_i1WgMNCvtekkXnrrPcAI-5hLjjkX9_RKAu1hTzClY2oqdeDO8MFgQLhN9vdYP-rohIHRBhEtf1ngJCKD8wKTf6sdJ19ARMwLR_RlxzKxmo9dMtsvlfjB1PFrQKvXlNnZVkDFfPWs7qjLUIORc6iBnimoZnCnrEVr4xFMi27u1v0XpKxspiawzwMC6ozRoH7R1Pe2mZ0VW_Gmx-7V_vnKS_IlTK5lShKgrosDgeZqgwk2R7rOcUoJyNuh_ohcgvBBz2zlG4Y',
    lastMessage: '谢谢你的学业互助，真的很有用！',
    time: '14:20',
    unreadCount: 1,
    messages: [
      {
        id: 'fox-1',
        sender: 'them',
        text: '你好！我在树洞看到你发的关于考研资料的帖子，请问数学三的笔记还在吗？',
        time: '昨天 18:30',
      },
      {
        id: 'fox-2',
        sender: 'me',
        text: '在的，还没被领走。如果你需要的话，明天中午我们可以约在信息学部食堂门口。',
        time: '昨天 19:02',
      },
      {
        id: 'fox-3',
        sender: 'them',
        text: '太好了！那明天 12:30 可以吗？谢谢你的学业互助，真的很有用！',
        time: '14:18',
      },
    ],
  },
  {
    id: 'museum',
    name: '信管男神（自封）',
    subtitle: '周末逛展搭子',
    avatar:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuCG-L6z8GchBOVwcTa9fg_eG6Dv1Ez_9cv7md9k1NUlXfk2o0Fn8b5Vn17vMeyuWv-yHXX4BkJemrZJCqoZ8_t4tuuSXHZzleJ5XEkpjeXHcHubBdyzxh8ig2gtGtlbG5ztNADSym8-pKzyjF1EoL4kIySj3blrLpYAgVFcqU7s5h4F8HYuq0bJTqVANynjNVd1O5eQe-5Xw8cSKP62DLY9UHeXQ7GgfOo01U2ArfbH9SSozv9GEVbBCVijB6zShOYccJ4Vsb0k8sU',
    lastMessage: '下次一起去万林博物馆看展吗？',
    time: '昨天',
    unreadCount: 0,
    messages: [
      { id: 'museum-1', sender: 'them', text: '这周万林的新展我已经想去三次了。', time: '星期一' },
      { id: 'museum-2', sender: 'me', text: '如果周末天气好，我们可以顺便去东湖边走走。', time: '星期一' },
    ],
  },
  {
    id: 'seat',
    name: '图书馆占座狂魔',
    subtitle: '自习室情报官',
    avatar:
      'https://lh3.googleusercontent.com/aida-public/AB6AXuCobU1o-AQ9F-xXIcMYHDZimsWnCqag814c1tf17Up_O0o5b11mw6-bSRGzzkXrFrkbk0Ht2yoCmP77WRJykeD2C2l-JsHNTB2zzohXWD6cQ2ukmyDgyw-SgxbMEwEjfKJgn1GJuZWHGiEqeigufQUVGZzyOvrDFw5hCOKJuxthE5abCY2sqcfS8DFaeS5kg4m-ll4BIrY3S6a_tFgrxPOE4CurCja-BKb8cWB-K7IZtAl8-uITsrnRO4hrrl_NmvvtCZkwsfwZiog',
    lastMessage: '不好意思，那个座位已经有人了。',
    time: '星期一',
    unreadCount: 0,
    messages: [
      { id: 'seat-1', sender: 'them', text: '今天总馆二楼靠窗的位置开放得比平时早。', time: '星期一' },
      { id: 'seat-2', sender: 'me', text: '收到，我下次试试看提前一点去。', time: '星期一' },
    ],
  },
];
